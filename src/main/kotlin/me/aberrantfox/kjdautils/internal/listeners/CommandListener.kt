package me.aberrantfox.kjdautils.internal.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.api.dsl.PrefixDeleteMode
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.deleteIfExists
import me.aberrantfox.kjdautils.extensions.jda.descriptor
import me.aberrantfox.kjdautils.extensions.jda.isCommandInvocation
import me.aberrantfox.kjdautils.extensions.jda.message
import me.aberrantfox.kjdautils.extensions.jda.messageTimed
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.command.CommandExecutor
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

internal class CommandListener(val config: KConfiguration,
                               val container: CommandsContainer,
                               var log: BotLogger,
                               val discord: Discord,
                               private val executor: CommandExecutor,
                               private val preconditions: MutableList<PreconditionData> = mutableListOf()) {

    @Subscribe
    fun guildMessageHandler(event: GuildMessageReceivedEvent) {
        val author = event.author
        val channel = event.channel
        val message = event.message

        if (author.isBot) return

        if (message.contentRaw.trimToID() == channel.jda.selfUser.id) {
            val mentionEmbed = discord.configuration.mentionEmbed?.invoke(event) ?: return
            channel.sendMessage(mentionEmbed).queue()
            return
        }

        handleMessage(channel, message, author, event.guild)
    }

    @Subscribe
    fun privateMessageHandler(e: PrivateMessageReceivedEvent) =
            handleMessage(e.channel, e.message, e.author)

    fun addPreconditions(vararg conditions: PreconditionData) = preconditions.addAll(conditions)

    private fun handleMessage(channel: MessageChannel, message: Message, author: User, guild: Guild? = null) {
        if (!isUsableCommand(message)) return

        val commandStruct = cleanCommandMessage(message.contentRaw, config)
        val (commandName, actualArgs, isDoubleInvocation) = commandStruct
        if (commandName.isEmpty())
            return

        val invokedInGuild = guild != null
        val shouldDelete = invokedInGuild && when (config.deleteMode){
            PrefixDeleteMode.Single -> !isDoubleInvocation
            PrefixDeleteMode.Double ->  isDoubleInvocation
            PrefixDeleteMode.None   ->  false
        }

        val event = CommandEvent(commandStruct, message, actualArgs, container,
                discord = discord,
                stealthInvocation = shouldDelete,
                guild = guild
        )

        getPreconditionError(event)?.let {
            if (it != "") {
                if(config.deleteErrors) event.respondTimed(it)
                else event.respond(it)
            }
            return
        }

        val command = container[commandName]

        if (command == null) {
            val recommended = CommandRecommender.recommendCommand(commandName) { config.visibilityPredicate(it, author, channel, guild) }
            val cleanName = commandName.sanitiseMentions()

            if (shouldDelete) message.deleteIfExists()
            val errorMsg = "I don't know what $cleanName is, perhaps you meant $recommended?"
            if(config.deleteErrors) channel.messageTimed(errorMsg)
            else channel.message(errorMsg)
            return
        }

        if (command.requiresGuild && !invokedInGuild) {
            val errorMsg = "This command must be invoked in a guild channel and not through PM"
            if(config.deleteErrors) channel.messageTimed(errorMsg)
            else channel.message(errorMsg)
            return
        }

        executor.executeCommand(command, actualArgs, event)

        if (!shouldDelete && config.reactToCommands) message.addReaction("\uD83D\uDC40").queue()


        log.cmd("${author.descriptor()} -- invoked $commandName in ${channel.name}")

        if (shouldDelete) message.deleteIfExists()
    }

    private fun isUsableCommand(message: Message): Boolean {
        if (message.contentRaw.length > 1500) return false

        if (!message.isCommandInvocation(config)) return false

        if (!config.allowPrivateMessages && message.channelType == ChannelType.PRIVATE) return false

        return true
    }

    private fun getPreconditionError(event: CommandEvent): String? {
        val sortedConditions = preconditions
                .groupBy({ it.priority }, { it.condition })
                .toList()
                .sortedBy { (priority, conditions) -> priority }
                .map { (priority, conditions) -> conditions }


        // Lazy sequence allows lower priorities to assume higher priorities are already verified
        val failedResults = sortedConditions.asSequence()
                .map { conditions -> conditions.map { it.invoke(event) } }
                .firstOrNull { results -> results.any { it is Fail } }
                ?.filterIsInstance<Fail>()

        return if (failedResults?.any { it.reason == null } == true) {
            ""
        } else {
            failedResults?.firstOrNull { it.reason != null }?.reason
        }
    }
}

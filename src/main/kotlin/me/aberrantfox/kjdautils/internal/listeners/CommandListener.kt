package me.aberrantfox.kjdautils.internal.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.*
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

internal class CommandListener(val config: KConfiguration,
                               val container: CommandsContainer,
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

        val discordContext = DiscordContext(shouldDelete, discord, message, author, channel, guild)
        val event = CommandEvent<ArgumentContainer>(commandStruct, container, discordContext)

        getPreconditionError(event)?.let {
            if (it != "") {
                if(config.deleteErrors) event.respondTimed(it)
                else event.respond(it)
            }
            return
        }

        val command = container[commandName]

        if (command == null) {
            val errorEmbed = CommandRecommender.buildRecommendationEmbed(commandName) {
                config.visibilityPredicate(it, author, channel, guild)
            }

            if (shouldDelete) message.deleteIfExists()
            if(config.deleteErrors) event.respondTimed(errorEmbed)
            else event.respond(errorEmbed)
            return
        }

        if (command.requiresGuild && !invokedInGuild) {
            val errorMsg = "This command must be invoked in a guild channel and not through PM"
            if(config.deleteErrors) event.respondTimed(errorMsg)
            else event.respond(errorMsg)
            return
        }

        executor.executeCommand(command, actualArgs, event)

        if (!shouldDelete && config.reactToCommands) message.addReaction("\uD83D\uDC40").queue()

        if (shouldDelete) message.deleteIfExists()
    }

    private fun isUsableCommand(message: Message): Boolean {
        if (message.contentRaw.length > 1500) return false

        if (!message.isCommandInvocation(config)) return false

        if (!config.allowPrivateMessages && message.channelType == ChannelType.PRIVATE) return false

        return true
    }

    private fun getPreconditionError(event: CommandEvent<*>): String? {
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

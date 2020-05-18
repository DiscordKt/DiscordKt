package me.aberrantfox.kjdautils.internal.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.services.ConversationService
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

internal class CommandListener(private val container: CommandsContainer,
                               private val discord: Discord,
                               private val conversationService: ConversationService,
                               private val preconditions: MutableList<PreconditionData> = mutableListOf()) {

    private val config = discord.configuration
    private val executor = CommandExecutor()

    @Subscribe
    fun guildMessageHandler(e: GuildMessageReceivedEvent) {
        val author = e.author
        val channel = e.channel
        val message = e.message

        if (author.isBot) return

        if (message.contentRaw.trimToID() == channel.jda.selfUser.id) {
            val mentionEmbed = discord.configuration.mentionEmbed?.invoke(DiscordContext(discord, message)) ?: return
            channel.sendMessage(mentionEmbed).queue()
            return
        }

        handleMessage(channel, message, author, e.guild)
    }

    @Subscribe
    fun privateMessageHandler(e: PrivateMessageReceivedEvent) =
            handleMessage(e.channel, e.message, e.author)

    private fun handleMessage(channel: MessageChannel, message: Message, author: User, guild: Guild? = null) {
        val content = message.contentRaw
        val discordContext = DiscordContext(discord, message)
        val prefix = config.prefix.invoke(discordContext)

        val rawInputs = when {
            isPrefixInvocation(content, prefix) -> stripPrefixInvocation(content, prefix)
            isMentionInvocation(content) -> stripMentionInvocation(content)
            else -> return conversationService.handleResponse(message)
        }

        val (_, commandName, actualArgs, _) = rawInputs

        if (!config.allowPrivateMessages && message.channelType == ChannelType.PRIVATE) return

        if (commandName.isEmpty())
            return

        val event = CommandEvent<GenericContainer>(rawInputs, container, discordContext)

        getPreconditionError(event)?.let {
            if (it != "") {
                if (config.deleteErrors) event.respondTimed(it)
                else event.respond(it)
            }
            return
        }

        val command = container[commandName]

        if (command == null) {
            val errorEmbed = CommandRecommender.buildRecommendationEmbed(commandName) {
                config.visibilityPredicate(it, author, channel, guild)
            }

            if(config.deleteErrors) event.respondTimed(errorEmbed)
            else event.respond(errorEmbed)
            return
        }

        executor.executeCommand(command, actualArgs, event)

        if (config.commandReaction != null)
            message.addReaction(config.commandReaction!!).queue()
    }

    private fun isPrefixInvocation(message: String, prefix: String) = message.startsWith(prefix)

    private fun isMentionInvocation(message: String): Boolean {
        if (!config.allowMentionPrefix)
            return false

        val id = discord.jda.selfUser.id

        return message.startsWith("<@!$id>") || message.startsWith("<@$id>")
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

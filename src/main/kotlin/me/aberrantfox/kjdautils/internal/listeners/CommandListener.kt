package me.aberrantfox.kjdautils.internal.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.api.getInjectionObject
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.services.ConversationService
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

internal class CommandListener(private val container: CommandsContainer,
                               private val discord: Discord,
                               private val preconditions: MutableList<PreconditionData> = mutableListOf()) {

    private val config = discord.configuration
    private val executor = CommandExecutor()

    @Subscribe
    fun guildMessageHandler(e: GuildMessageReceivedEvent) = handleMessage(e.message)

    @Subscribe
    fun privateMessageHandler(e: PrivateMessageReceivedEvent) = handleMessage(e.message)

    private fun handleMessage(message: Message) {
        val author = message.author
        val channel = message.channel
        val guild = if (message.isFromGuild) message.guild else null

        if (author.isBot) return

        val content = message.contentRaw
        val discordContext = DiscordContext(discord, message)
        val prefix = config.prefix.invoke(discordContext)

        val conversationService = discord.getInjectionObject<ConversationService>()!!

        val rawInputs = when {
            isPrefixInvocation(content, prefix) -> stripPrefixInvocation(content, prefix)
            content.trimToID() == channel.jda.selfUser.id -> {
                val embed = config.mentionEmbed?.invoke(discordContext) ?: return
                return channel.sendMessage(embed).queue()
            }
            isMentionInvocation(content) -> stripMentionInvocation(content)
            else -> return conversationService.handleResponse(message)
        }

        val (_, commandName, actualArgs, _) = rawInputs

        if (commandName.isEmpty()) return

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

            if (config.deleteErrors) event.respondTimed(errorEmbed)
            else event.respond(errorEmbed)
            return
        }

        if (!message.isFromGuild)
            if (command.requiresGuild ?: config.requiresGuild)
                return

        if (config.commandReaction != null)
            message.addReaction(config.commandReaction!!).queue()

        executor.executeCommand(command, actualArgs, event)
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
            .sortedBy { (priority, _) -> priority }
            .map { (_, conditions) -> conditions }

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

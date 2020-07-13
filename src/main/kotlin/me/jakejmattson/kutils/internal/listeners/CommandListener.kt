package me.jakejmattson.kutils.internal.listeners

import com.google.common.eventbus.Subscribe
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.dsl.configuration.BotConfiguration
import me.jakejmattson.kutils.api.dsl.preconditions.*
import me.jakejmattson.kutils.api.extensions.stdlib.trimToID
import me.jakejmattson.kutils.api.services.ConversationService
import me.jakejmattson.kutils.internal.command.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

internal class CommandListener(private val container: CommandsContainer,
                               private val discord: Discord,
                               private val preconditions: List<Precondition> = listOf()) {

    private val config: BotConfiguration = discord.configuration

    @Subscribe
    fun guildMessageHandler(e: GuildMessageReceivedEvent) = handleMessage(e.message)

    @Subscribe
    fun privateMessageHandler(e: PrivateMessageReceivedEvent) = handleMessage(e.message)

    private fun handleMessage(message: Message) {
        val author = message.author.takeUnless { it.isBot } ?: return
        val channel = message.channel
        val content = message.contentRaw
        val discordContext = DiscordContext(discord, message)
        val prefix = config.prefix.invoke(discordContext)
        val conversationService = discord.getInjectionObjects(ConversationService::class)

        val rawInputs = when {
            isPrefixInvocation(content, prefix) -> stripPrefixInvocation(content, prefix)
            content.trimToID() == channel.jda.selfUser.id -> {
                val embed = config.mentionEmbed?.invoke(discordContext) ?: return
                return channel.sendMessage(embed).queue()
            }
            isMentionInvocation(content) -> stripMentionInvocation(content)
            else -> return conversationService.handleMessage(message)
        }

        val (_, commandName, actualArgs, _) = rawInputs

        if (commandName.isEmpty()) return

        val event = CommandEvent<GenericContainer>(rawInputs, container, discordContext)
        val errors = getPreconditionErrors(event)

        if (errors.isNotEmpty()) {
            val errorMessage = errors.firstOrNull { it.isNotBlank() }

            if (errorMessage != null)
                if (config.deleteErrors) event.respondTimed(errorMessage) else event.respond(errorMessage)

            return
        }

        val command = container[commandName]

        if (command == null) {
            val guild = if (message.isFromGuild) message.guild else null

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

        command.invoke(actualArgs, event)
    }

    private fun isPrefixInvocation(message: String, prefix: String) = message.startsWith(prefix)

    private fun isMentionInvocation(message: String): Boolean {
        if (!config.allowMentionPrefix)
            return false

        val id = discord.jda.selfUser.id

        return message.startsWith("<@!$id>") || message.startsWith("<@$id>")
    }

    private fun getPreconditionErrors(event: CommandEvent<*>) =
        preconditions
            .map { it.evaluate(event) }
            .filterIsInstance<Fail>()
            .map { it.reason }
}
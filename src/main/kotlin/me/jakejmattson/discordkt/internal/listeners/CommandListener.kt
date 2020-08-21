package me.jakejmattson.discordkt.internal.listeners

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.on
import com.gitlab.kordlib.kordx.emoji.toReaction
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.api.dsl.preconditions.*
import me.jakejmattson.discordkt.api.extensions.trimToID
import me.jakejmattson.discordkt.api.services.ConversationService
import me.jakejmattson.discordkt.internal.command.*
import me.jakejmattson.discordkt.internal.utils.Recommender

internal suspend fun registerCommandListener(discord: Discord, preconditions: List<Precondition>) = discord.api.on<MessageCreateEvent> {
    val config = discord.configuration

    fun isMentionInvocation(message: String): Boolean {
        if (!config.allowMentionPrefix)
            return false

        val id = kord.selfId

        return message.startsWith("<@!$id>") || message.startsWith("<@$id>")
    }

    val author = message.author ?: return@on
    val discordContext = DiscordContext(discord, message, getGuild())
    val prefix = config.prefix.invoke(discordContext)
    val conversationService = discord.getInjectionObjects(ConversationService::class)
    val channel = message.channel
    val content = message.content

    val rawInputs = when {
        content.startsWith(prefix) -> stripPrefixInvocation(content, prefix)
        content.trimToID() == kord.selfId.toString() -> {
            config.mentionEmbed?.let {
                channel.createEmbed {
                    it.invoke(this, discordContext)
                }
            }

            return@on
        }
        isMentionInvocation(content) -> stripMentionInvocation(content)
        else -> return@on conversationService.handleMessage(message)
    }

    val (_, commandName, actualArgs, _) = rawInputs

    if (commandName.isEmpty()) return@on

    val event = CommandEvent<GenericContainer>(rawInputs, discordContext)
    val errors = preconditions
        .map { it.evaluate(event) }
        .filterIsInstance<Fail>()
        .map { it.reason }

    if (errors.isNotEmpty()) {
        val errorMessage = errors.firstOrNull { it.isNotBlank() }

        if (errorMessage != null)
            event.respond(errorMessage)

        return@on
    }

    val command = discord.commands[commandName]?.takeUnless { !config.hasPermission(it, author, channel) }

    if (command == null) {
        val validCommands = discord.commands
            .filter { config.hasPermission(it, author, channel) }
            .flatMap { it.names }

        return@on Recommender.sendRecommendation(event, commandName, validCommands)
    }

    if (message.getGuildOrNull() == null)
        if (command.requiresGuild ?: config.requiresGuild)
            return@on

    config.commandReaction.let {
        message.addReaction(it!!.toReaction())
    }

    command.invoke(event, actualArgs)
}
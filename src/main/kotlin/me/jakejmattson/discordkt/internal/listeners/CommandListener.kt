package me.jakejmattson.discordkt.internal.listeners

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.channel.*
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.on
import com.gitlab.kordlib.kordx.emoji.toReaction
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.extensions.trimToID
import me.jakejmattson.discordkt.api.services.ConversationService
import me.jakejmattson.discordkt.internal.command.*
import me.jakejmattson.discordkt.internal.utils.Recommender

internal suspend fun registerCommandListener(discord: Discord) = discord.api.on<MessageCreateEvent> {
    val config = discord.configuration
    val self = kord.selfId.longValue
    val author = message.author ?: return@on
    val discordContext = DiscordContext(discord, message, getGuild())
    val prefix = config.prefix.invoke(discordContext)
    val conversationService = discord.getInjectionObjects(ConversationService::class)
    val channel = message.channel.asChannel()
    val content = message.content

    fun String.mentionsSelf() = startsWith("<@!$self>") || startsWith("<@$self>")

    val rawInputs = when {
        content.startsWith(prefix) -> stripPrefixInvocation(content, prefix)
        content.trimToID() == self.toString() -> {
            config.mentionEmbed?.let {
                channel.createEmbed {
                    it.invoke(this, discordContext)
                }
            }

            return@on
        }
        content.mentionsSelf() && config.allowMentionPrefix -> stripMentionInvocation(content)
        else -> return@on conversationService.handleMessage(message)
    }

    val (_, commandName, commandArgs, _) = rawInputs

    if (commandName.isBlank()) return@on

    val guild = getGuild()

    val event = if (guild != null)
        GuildCommandEvent<GenericContainer>(rawInputs, discord, message, author, channel as TextChannel, guild)
    else
        DmCommandEvent(rawInputs, discord, message, author, channel as DmChannel)

    val errors = discord.preconditions
        .mapNotNull {
            try {
                it.check(event)
                null
            }
            catch (e: Exception) {
                e.message ?: ""
            }
        }

    if (errors.isNotEmpty()) {
        errors.firstOrNull { it.isNotBlank() }?.let { event.respond(it) }
        return@on
    }

    val command = discord.commands[commandName]?.takeUnless { !config.hasPermission(it, event) }

    if (command == null) {
        val validCommands = discord.commands
            .filter { config.hasPermission(it, event) }
            .flatMap { it.names }

        return@on Recommender.sendRecommendation(event, commandName, validCommands)
    }

    config.commandReaction?.let {
        message.addReaction(it.toReaction())
    }

    command.invoke(event, commandArgs)
}
package me.jakejmattson.discordkt.internal.listeners

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.channel.*
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.on
import com.gitlab.kordlib.kordx.emoji.addReaction
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.extensions.trimToID
import me.jakejmattson.discordkt.internal.command.*
import me.jakejmattson.discordkt.internal.utils.Recommender

internal suspend fun registerCommandListener(discord: Discord) = discord.api.on<MessageCreateEvent> {
    val config = discord.configuration
    val self = kord.selfId.value
    val author = message.author ?: return@on
    val discordContext = DiscordContext(discord, message, getGuild())
    val prefix = config.prefix.invoke(discordContext)
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
        else -> return@on Conversations.handleMessage(message)
    }

    val (_, commandName, commandArgs, _) = rawInputs

    if (commandName.isBlank()) return@on

    val event = getGuild()?.let {
        GuildCommandEvent<TypeContainer>(rawInputs, discord, message, author, channel as TextChannel, it)
    } ?: DmCommandEvent(rawInputs, discord, message, author, channel as DmChannel)

    //Apply preconditions
    discord.preconditions
        .sortedBy { it.priority }
        .forEach {
            try {
                it.check(event)
            } catch (e: Exception) {
                e.message.takeUnless { it.isNullOrEmpty() }?.let { event.respond(it) }
                return@on
            }
        }

    val command = discord.commands[commandName]?.takeUnless { !config.hasPermission(it, event) }
        ?: return@on Recommender.sendRecommendation(event, commandName)

    config.commandReaction?.let {
        message.addReaction(it)
    }

    command.invoke(event, commandArgs)
}
package me.jakejmattson.discordkt.internal.listeners

import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.conversations.Conversations
import me.jakejmattson.discordkt.util.trimToID

internal suspend fun registerCommandListener(discord: Discord) = discord.kord.on<MessageCreateEvent> {
    val config = discord.configuration
    val self = kord.selfId.value
    val author = message.author ?: return@on
    val guild = getGuildOrNull()
    val context = DiscordContext(discord, message, author, message.channel, guild)
    val channel = message.channel.asChannel()
    val content = message.content

    if (content.trimToID() == self.toString()) {
        config.mentionEmbed.second?.let {
            channel.createEmbed {
                it.invoke(this, context)
            }
        }
    } else Conversations.handleMessage(message)
}

internal suspend fun arePreconditionsPassing(event: CommandEvent<*>): Boolean {
    event.discord.preconditions
        .sortedBy { it.priority }
        .forEach { precondition ->
            try {
                precondition.check(event)
            } catch (e: Exception) {
                e.message.takeUnless { it.isNullOrEmpty() }?.let { event.respond(it) }
                return false
            }
        }

    return true
}
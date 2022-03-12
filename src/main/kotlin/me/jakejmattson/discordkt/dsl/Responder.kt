@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.extensions.sanitiseMentions

/**
 * An interface for responding to input in a given context.
 */
public interface Responder {
    /**
     * The channel that this entity was invoked in.
     */
    public val channel: MessageChannelBehavior

    /**
     * Send this message with no sanitization.
     */
    public suspend fun respond(message: Any): List<Message> = chunkRespond(message.toString())

    /**
     * Respond with a message and sanitize mentions.
     */
    public suspend fun safeRespond(discord: Discord, message: Any): List<Message> = chunkRespond(message.toString().sanitiseMentions(discord))

    /**
     * Respond with an embed.
     */
    public suspend fun respond(embedBuilder: suspend EmbedBuilder.() -> Unit): Message? = channel.createEmbed { embedBuilder.invoke(this) }

    /**
     * Respond with a message and an embed.
     */
    public suspend fun respond(message: String, embedBuilder: suspend EmbedBuilder.() -> Unit): Message? = channel.createMessage {
        content = message
        embedBuilder.invoke(embeds.first())
    }

    /**
     * Respond with a [Menu].
     */
    public suspend fun respondMenu(menuBuilder: suspend MenuBuilder.() -> Unit): Message {
        val handle = MenuBuilder()
        handle.menuBuilder()
        return handle.build().send(channel)
    }

    private suspend fun chunkRespond(message: String): List<Message> {
        require(message.isNotEmpty()) { "Cannot send an empty message." }
        return message.chunked(2000).map { channel.createMessage(it) }
    }
}

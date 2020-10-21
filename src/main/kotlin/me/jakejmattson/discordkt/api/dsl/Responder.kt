@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.core.behavior.channel.*
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.extensions.sanitiseMentions

/**
 * An interface for responding to input in a given context.
 */
interface Responder {
    /**
     * The channel that this entity was invoked in.
     */
    val channel: MessageChannelBehavior

    /**
     * Send this message with no sanitization.
     */
    suspend fun respond(message: Any) = chunkRespond(message.toString())

    /**
     * Respond with a message and sanitize mentions.
     */
    suspend fun safeRespond(discord: Discord, message: Any) = chunkRespond(message.toString().sanitiseMentions(discord))

    /**
     * Respond with an embed.
     */
    suspend fun respond(construct: suspend EmbedBuilder.() -> Unit) = channel.createEmbed { construct.invoke(this) }

    /**
     * Respond with a message and an embed.
     */
    suspend fun respond(message: String, construct: suspend EmbedBuilder.() -> Unit) = channel.createMessage {
        content = message
        construct.invoke(embed!!)
    }

    /**
     * Respond with a menu.
     */
    suspend fun respondMenu(construct: suspend MenuBuilder.() -> Unit): Message? {
        val handle = MenuBuilder()
        handle.construct()
        return handle.build().send(channel)
    }

    private suspend fun chunkRespond(message: String): List<Message> {
        require(message.isNotEmpty()) { "Cannot send an empty message." }
        return message.chunked(2000).map { channel.createMessage(it) }
    }
}

@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.InteractionResponseBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed

/**
 * An interface for responding to input in a given context.
 */
public interface Responder {
    /**
     * The channel that this entity was invoked in.
     */
    public val channel: MessageChannelBehavior

    /**
     * Create a response message with text and/or an embed.
     *
     * @param message Message text content.
     * @param embed Message embed content.
     */
    public suspend fun respond(message: Any = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): Message? = channel.createMessage {
        val responseContent = message.toString()

        if (responseContent.isNotEmpty())
            content = responseContent

        if (embed != null)
            embed { embed.invoke(this) }
    }

    /**
     * Respond with a [Menu].
     */
    public suspend fun respondMenu(menuBuilder: suspend MenuBuilder.() -> Unit): Message {
        val handle = MenuBuilder()
        handle.menuBuilder()
        return handle.build().send(channel)
    }
}

public interface SlashResponder : Responder {
    public val interaction: ApplicationCommandInteraction?

    /**
     * Create an ephemeral slash response with text and/or an embed.
     *
     * @param message Message text content.
     * @param embed Message embed content.
     */
    override suspend fun respond(message: Any, embed: (suspend EmbedBuilder.() -> Unit)?): Message? =
        if (interaction != null) {
            interaction!!.respondEphemeral {
                val responseContent = message.toString()

                if (responseContent.isNotEmpty())
                    content = responseContent

                if (embed != null)
                    embed { embed.invoke(this) }
            }
            null
        } else
            super.respond(message, embed)

    /**
     * Create a public slash response with text and/or an embed.
     *
     * @param message Message text content.
     * @param embed Message embed content.
     */
    public suspend fun respondPublic(message: Any = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): InteractionResponseBehavior? =
        if (interaction != null) {
            interaction!!.respondPublic {
                val responseContent = message.toString()

                if (responseContent.isNotEmpty())
                    content = responseContent

                if (embed != null)
                    embed { embed.invoke(this) }
            }
        } else {
            super.respond(message, embed)
            null
        }
}

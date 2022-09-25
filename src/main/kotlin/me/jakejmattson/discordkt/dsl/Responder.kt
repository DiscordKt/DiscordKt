@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed

/**
 * A modified responder for slash commands.
 */
public interface SlashResponder {
    /**
     * The interaction to respond from.
     */
    public val interaction: ApplicationCommandInteraction

    /**
     * Create an ephemeral slash response with text and/or an embed.
     *
     * @param message Message text content.
     * @param embed Message embed content.
     */
    public suspend fun respond(message: Any = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): EphemeralMessageInteractionResponseBehavior =
        interaction.respondEphemeral {
            val responseContent = message.toString()

            if (responseContent.isNotEmpty())
                content = responseContent

            if (embed != null)
                embed { embed.invoke(this) }
        }

    /**
     * Create a public slash response with text and/or an embed.
     *
     * @param message Message text content.
     * @param embed Message embed content.
     */
    public suspend fun respondPublic(message: Any = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): PublicMessageInteractionResponseBehavior =
        interaction.respondPublic {
            val responseContent = message.toString()

            if (responseContent.isNotEmpty())
                content = responseContent

            if (embed != null)
                embed { embed.invoke(this) }
        }


    //TODO slash menus
    /**
     * Respond with a [Menu].
     */
    /**
    public suspend fun respondMenu(menuBuilder: suspend MenuBuilder.() -> Unit): Message {
        val handle = MenuBuilder()
        handle.menuBuilder()
        return handle.build().send(channel)
    }
    */
}

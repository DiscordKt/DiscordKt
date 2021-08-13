package me.jakejmattson.discordkt.api.conversations

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.api.extensions.toPartialEmoji
import java.util.*

internal class ConversationButton<T>(
    val label: String?,
    val emoji: ReactionEmoji?,
    val id: String,
    val value: T,

    @OptIn(KordPreview::class)
    val style: ButtonStyle
)

/**
 * Builder for a button prompt
 */
class ButtonPromptBuilder<T> {
    private lateinit var promptEmbed: suspend EmbedBuilder.() -> Unit
    private val buttonRows: MutableList<MutableList<ConversationButton<T>>> = mutableListOf()

    internal val valueMap: Map<String, T>
        get() = buttonRows.flatten().associate { it.id to it.value }

    /**
     * Create the embed prompting the user for input.
     */
    fun embed(prompt: suspend EmbedBuilder.() -> Unit) {
        promptEmbed = prompt
    }

    /**
     * Create a new row of buttons using the [button][ConversationButtonRowBuilder.button] builder.
     */
    fun buttons(rowBuilder: ConversationButtonRowBuilder<T>.() -> Unit) {
        val builder = ConversationButtonRowBuilder<T>()
        rowBuilder.invoke(builder)
        buttonRows.add(builder.buttons)
    }

    @OptIn(KordPreview::class)
    internal suspend fun create(channel: MessageChannel) = channel.createMessage {
        this.embed {
            promptEmbed.invoke(this)
        }

        buttonRows.forEach { buttons ->
            actionRow {
                buttons.forEach { button ->
                    interactionButton(button.style, button.id) {
                        this.label = button.label
                        this.emoji = button.emoji?.toPartialEmoji()
                    }
                }
            }
        }
    }
}

/**
 * Builder functions for conversation buttons.
 */
class ConversationButtonRowBuilder<T> {
    internal val buttons = mutableListOf<ConversationButton<T>>()

    /**
     * A Discord button component.
     * Exposes the menu for navigation functions.
     *
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param value The value returned when this button is pressed
     * @param style The Button [style][ButtonStyle]
     */
    @OptIn(KordPreview::class)
    fun button(label: String?, emoji: DiscordEmoji?, value: T, style: ButtonStyle = ButtonStyle.Secondary) {
        val button = ConversationButton(label, emoji?.toReaction(), UUID.randomUUID().toString(), value, style)
        buttons.add(button)
    }
}
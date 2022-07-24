package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.SlashCommandEvent
import me.jakejmattson.discordkt.dsl.uuid
import me.jakejmattson.discordkt.extensions.toPartialEmoji

public class ConversationButton<T>(
    public val label: String?,
    public val emoji: ReactionEmoji?,
    public val id: String,
    public val value: T,
    public val style: ButtonStyle
)

/**
 * Builder for a button prompt
 */
public open class ButtonPromptBuilder<T> {
    protected lateinit var promptEmbed: suspend EmbedBuilder.() -> Unit
    protected val buttonRows: MutableList<MutableList<ConversationButton<T>>> = mutableListOf()

    internal val valueMap: Map<String, T>
        get() = buttonRows.flatten().associate { it.id to it.value }

    /**
     * Create the embed prompting the user for input.
     */
    public fun embed(prompt: suspend EmbedBuilder.() -> Unit) {
        promptEmbed = prompt
    }

    /**
     * Create a new row of buttons using the [button][ConversationButtonRowBuilder.button] builder.
     */
    public fun buttons(rowBuilder: ConversationButtonRowBuilder<T>.() -> Unit) {
        val builder = ConversationButtonRowBuilder<T>()
        rowBuilder.invoke(builder)
        buttonRows.add(builder.buttons)
    }

    internal suspend fun create(channel: MessageChannel) = channel.createMessage {
        createMessage(this)
    }

    protected suspend inline fun createMessage(messageBuilder: MessageCreateBuilder) {
        with(messageBuilder) {
            embed {
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
}

public class OneOf<T : Any, U : Any> private constructor(public val first: T?, public val second: U?) {
    public companion object {
        public fun <T : Any, U : Any> first(first: T): OneOf<T, U> =
            OneOf(first, null)

        public fun <T : Any, U : Any> second(second: U): OneOf<T, U> =
            OneOf(null, second)
    }
}

/**
 * Builder for a buttom prompt in a slash conversation
 */
public class SlashButtonPromptBuilder<T, U : TypeContainer> : ButtonPromptBuilder<T>() {
    internal suspend fun create(event: SlashCommandEvent<U>, botResponse: PublicMessageInteractionResponseBehavior? = null): OneOf<PublicMessageInteractionResponseBehavior, PublicFollowupMessage> {
        return if (botResponse == null) {
            OneOf.first(event.interaction!!.respondPublic {
                createMessage(this)
            })
        } else {
            OneOf.second(botResponse.createPublicFollowup {
                createMessage(this)
            })
        }
    }
}

/**
 * Builder functions for conversation buttons.
 */
public class ConversationButtonRowBuilder<T> {
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
    public fun button(label: String?, emoji: DiscordEmoji?, value: T, style: ButtonStyle = ButtonStyle.Secondary) {
        val button = ConversationButton(label, emoji?.toReaction(), uuid(), value, style)
        buttons.add(button)
    }
}
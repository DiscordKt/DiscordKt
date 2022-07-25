package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import kotlinx.coroutines.runBlocking
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

    internal suspend fun create(channel: MessageChannel, message: Message): MessageResponder {
        val responder = ChannelResponder(channel, message)

        return responder.respond { createMessage(this) }
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

public interface MessageResponder {
    public val ofMessage: Message

    public suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): MessageResponder
}

public class ChannelResponder(private val channel: MessageChannel, private val message: Message) : MessageResponder {
    public override val ofMessage: Message = message

    override suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): MessageResponder {
        val newMessage = channel.createMessage {
            runBlocking { builder.invoke(this@createMessage) }
        }

        return ChannelResponder(channel, newMessage)
    }
}

public class FollowupResponder(
    public val botResponse: PublicMessageInteractionResponseBehavior,
    public val followupMessage: PublicFollowupMessage? = null,
) : MessageResponder {
    override val ofMessage: Message
        get() = runBlocking { // FIXME don't use runBlocking
            followupMessage?.message ?: getMessageOfBotResponse(botResponse.applicationId, botResponse.token)
        }

    private suspend fun getMessageOfBotResponse(applicationId: Snowflake, token: String): Message {
        val messageData = botResponse.kord.rest.interaction.getInteractionResponse(applicationId, token).toData()

        return Message(messageData, botResponse.kord)
    }

    override suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): MessageResponder {
        val newFollowupMessage = botResponse.createPublicFollowup {
            runBlocking { builder.invoke(this@createPublicFollowup) } // FIXME don't use runBlocking
        }

        return FollowupResponder(botResponse, newFollowupMessage)
    }
}

public class SlashResponder<T : TypeContainer>(private val event: SlashCommandEvent<T>) : MessageResponder {
    override val ofMessage: Message
        get() = runBlocking { getMessageOfBotResponse(event.interaction!!.applicationId, event.interaction!!.token) } // FIXME don't use runBlocking

    private suspend fun getMessageOfBotResponse(applicationId: Snowflake, token: String): Message {
        val messageData = event.discord.kord.rest.interaction.getInteractionResponse(applicationId, token).toData()

        return Message(messageData, event.discord.kord)
    }

    override suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): MessageResponder {
        val responseBehavior = event.interaction!!.respondPublic {
            runBlocking { builder.invoke(this@respondPublic) } // FIXME don't use runBlocking
        }

        return FollowupResponder(responseBehavior)
    }
}

/**
 * Builder for a buttom prompt in a slash conversation
 */
public class SlashButtonPromptBuilder<T, U : TypeContainer> : ButtonPromptBuilder<T>() {
    internal suspend fun create(event: SlashCommandEvent<U>, responder: MessageResponder?): MessageResponder {
        val actualResponder = responder ?: SlashResponder(event)

        return actualResponder.respond { createMessage(this) }
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
package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.conversations.responders.ConversationResponder
import me.jakejmattson.discordkt.prompts.SimpleSelectBuilder
import me.jakejmattson.discordkt.util.uuid
import java.util.*
import kotlin.concurrent.schedule

/**
 * DSL for building a conversation.
 */
public abstract class ConversationBuilder(
    public val discord: Discord,
    public val user: User,
    public val channel: MessageChannel,
    private val exitString: String? = null,
    private val timeout: Long,
) {
    private val messageBuffer = Channel<Message>()

    private val interactionBuffer = Channel<ComponentInteraction>()

    private val exceptionBuffer = Channel<TimeoutException>()

    /**
     * All responder objects that have been used to respond to user messages/interactions.
     */
    public val responders: MutableList<ConversationResponder> = mutableListOf()

    /**
     * All ID's of messages sent by the user in this conversation.
     */
    public abstract val userMessageIds: MutableList<Snowflake>

    /**
     * All ID's of messages sent by the bot in this conversation.
     */
    public abstract val botMessageIds: MutableList<Snowflake>

    /**
     * The ID of the most recent message sent by the user in this conversation.
     */
    public val previousUserMessageId: Snowflake
        get() = userMessageIds.last()

    /**
     * The ID of the most recent message sent by the bot in this conversation.
     */
    public val previousBotMessageId: Snowflake
        get() = botMessageIds.last()

    internal suspend fun acceptMessage(message: Message) = messageBuffer.send(message)

    internal suspend fun acceptInteraction(interaction: ComponentInteraction) = interactionBuffer.send(interaction)

    /**
     * Prompt the user with a String. Re-prompt until the response converts correctly. Then apply a custom predicate as an additional check.
     *
     * @param argument The [Argument] that the prompt expects in response.
     * @param prompt The string message sent to the user as a prompt for information.
     * @param error The error String to send when the input fails the custom check.
     * @param isValid A predicate to determine whether the input is accepted.
     */
    @Throws(DmException::class)
    public abstract suspend fun promptUntil(prompt: String, error: String, isValid: (String) -> Boolean): String

    /**
     * Prompt the user with text and/or embed.
     *
     * @param argument The [Argument] that the prompt expects in response.
     * @param text A String sent as part of the prompt.
     * @param embed The embed sent as part of the prompt.
     */
    @Throws(DmException::class, TimeoutException::class)
    public abstract suspend fun prompt(text: String = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): String

    /**
     * Prompt the user with an embed and the provided buttons.
     * Requires a call to both [ButtonPromptBuilder.embed] and [ButtonPromptBuilder.buttons].
     *
     * @param prompt The [builder][ButtonPromptBuilder]
     */
    @Throws(DmException::class, TimeoutException::class)
    public abstract suspend fun <T> promptButton(prompt: suspend ButtonPromptBuilder<T>.() -> Unit): T

    /**
     * Prompt the user with a select menu.
     *
     * @param builder Build a select menu with a [SimpleSelectBuilder].
     */
    @Throws(DmException::class, TimeoutException::class)
    public abstract suspend fun promptSelect(builder: SimpleSelectBuilder.() -> Unit): List<String>

    /**
     * Creates the promptSelect message inside the specified [MessageCreateBuilder].
     */
    internal fun createSelectMessage(select: SimpleSelectBuilder.() -> Unit, builder: MessageCreateBuilder) {
        val selectBuilder = SimpleSelectBuilder()
        select.invoke(selectBuilder)

        with(builder) {
            content = selectBuilder.textContent
            selectBuilder.embedContent?.let { embed { it.invoke(this) } }

            actionRow {
                stringSelect(uuid()) {
                    this.allowedValues = selectBuilder.selectionCount

                    selectBuilder.options.forEach {
                        option(it.label, it.value) {
                            this.description = it.description
                            this.emoji = it.emoji
                        }
                    }
                }
            }
        }
    }

    protected fun retrieveValidTextResponse(): String = runBlocking {
        retrieveTextResponse() ?: retrieveValidTextResponse()
    }

    private suspend fun retrieveTextResponse(): String? = select {
        val timer = createTimer()

        exceptionBuffer.onReceive { timeoutException ->
            throw timeoutException
        }

        messageBuffer.onReceive { message ->
            userMessageIds.add(message.id)
            responders.lastOrNull()?.let { it.userResponse = message }

            if (message.content == exitString)
                throw ExitException()

            timer?.cancel()

            return@onReceive message.content
        }
    }

    protected fun <T> retrieveValidInteractionResponse(buttons: Map<String, T>): List<T> = runBlocking {
        retrieveInteractionResponse(buttons) ?: retrieveValidInteractionResponse(buttons)
    }

    protected abstract suspend fun interactionIsOnLastBotMessage(interaction: ComponentInteraction): Boolean

    private suspend fun <T> retrieveInteractionResponse(buttons: Map<String, T>): List<T>? = select {
        val timer = createTimer()

        exceptionBuffer.onReceive { timeoutException ->
            throw timeoutException
        }

        messageBuffer.onReceive { message ->
            if (message.content == exitString) {
                timer?.cancel()
                throw ExitException()
            } else
                null
        }

        interactionBuffer.onReceive { interaction ->
            if (!interactionIsOnLastBotMessage(interaction))
                return@onReceive null

            timer?.cancel()
            interaction.deferEphemeralMessageUpdate()

            if (interaction is SelectMenuInteraction)
                interaction.values as List<T>
            else
                listOf(buttons[interaction.componentId]!!)
        }
    }

    private fun createTimer(): TimerTask? =
        timeout.takeIf { it > 0 }?.let { time ->
            Timer("Timeout", false).schedule(time) {
                runBlocking { exceptionBuffer.send(TimeoutException()) }
            }
        }
}

internal class TimeoutException : Exception("Prompt not answered in time.")
internal class ExitException : Exception("Conversation exited early.")
internal class DmException : Exception("Message failed to deliver.")
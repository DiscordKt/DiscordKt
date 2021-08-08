package me.jakejmattson.discordkt.api.conversations

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.TypeContainer
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.RawInputs
import me.jakejmattson.discordkt.api.dsl.Responder
import java.util.*
import kotlin.concurrent.schedule

/** @suppress DSL backing
 *
 * @param discord The discord instance.
 * @param user The user that the conversation is happening with.
 * @param channel The channel that the conversation is happening in.
 * @param exitString A String entered by the user to exit the conversation.
 */
data class ConversationBuilder(val discord: Discord,
                               val user: User,
                               override val channel: MessageChannel,
                               private val exitString: String? = null,
                               private val timeout: Long) : Responder {
    private val messageBuffer = Channel<Message>()

    @OptIn(KordPreview::class)
    private val interactionBuffer = Channel<ComponentInteraction>()

    private val exceptionBuffer = Channel<TimeoutException>()

    /**
     * All ID's of messages sent by the bot in this conversation.
     */
    val botMessageIds = mutableListOf<Snowflake>()

    /**
     * All ID's of messages sent by the user in this conversation.
     */
    val userMessageIds = mutableListOf<Snowflake>()

    /**
     * The ID of the most recent message sent by the bot in this conversation.
     */
    val previousBotMessageId
        get() = botMessageIds.last()

    /**
     * The ID of the most recent message sent by the user in this conversation.
     */
    val previousUserMessageId
        get() = userMessageIds.last()

    internal suspend fun acceptMessage(message: Message) = messageBuffer.send(message)

    @OptIn(KordPreview::class)
    internal suspend fun acceptInteraction(interaction: ComponentInteraction) = interactionBuffer.send(interaction)

    /**
     * Prompt the user with a String. Re-prompt until the response converts correctly. Then apply a custom predicate as an additional check.
     *
     * @param argumentType The [ArgumentType] that the prompt expects in response.
     * @param prompt The string message sent to the user as a prompt for information.
     * @param error The error String to send when the input fails the custom check.
     * @param isValid A predicate to determine whether or not the input is accepted.
     */
    @Throws(DmException::class)
    suspend fun <T> promptUntil(argumentType: ArgumentType<T>, prompt: String, error: String, isValid: (T) -> Boolean): T {
        var value: T = promptMessage(argumentType, prompt)

        while (!isValid.invoke(value)) {
            channel.createMessage(error).also { it.let { botMessageIds.add(it.id) } }
            value = promptMessage(argumentType, prompt)
        }

        return value
    }

    /**
     * Prompt the user with a String. Re-prompt until the response converts correctly.
     *
     * @param argumentType The [ArgumentType] that the prompt expects in response.
     * @param prompt The string message sent to the user as a prompt for information.
     */
    @Throws(DmException::class, TimeoutException::class)
    fun <T> promptMessage(argumentType: ArgumentType<T>, prompt: String): T {
        require(argumentType !is OptionalArg<*>) { "Conversation arguments cannot be optional" }
        return retrieveValidTextResponse(argumentType, prompt)
    }

    /**
     * Prompt the user with an embed. Re-prompt until the response converts correctly.
     *
     * @param argumentType The [ArgumentType] that the prompt expects in response.
     * @param prompt The embed sent to the user as a prompt for information.
     */
    @Throws(DmException::class, TimeoutException::class)
    suspend fun <T> promptEmbed(argumentType: ArgumentType<T>, prompt: suspend EmbedBuilder.() -> Unit): T {
        require(argumentType !is OptionalArg<*>) { "Conversation arguments cannot be optional" }

        val message = channel.createEmbed {
            prompt.invoke(this)
        }

        botMessageIds.add(message.id)

        return retrieveValidTextResponse(argumentType, null)
    }

    /**
     * Prompt the user with an embed and the provided buttons.
     * Requires a call to both [ButtonPromptBuilder.embed] and [ButtonPromptBuilder.buttons].
     *
     * @param prompt The [builder][ButtonPromptBuilder]
     */
    @Throws(DmException::class, TimeoutException::class)
    suspend fun <T> promptButton(prompt: suspend ButtonPromptBuilder<T>.() -> Unit): T {
        val builder = ButtonPromptBuilder<T>()
        prompt.invoke(builder)
        val message = builder.create(channel)

        botMessageIds.add(message.id)

        return retrieveValidInteractionResponse(builder.valueMap)
    }

    private fun <T> retrieveValidTextResponse(argumentType: ArgumentType<T>, prompt: String?): T = runBlocking {
        prompt?.let { channel.createMessage(it) }?.also { botMessageIds.add(it.id) }
        retrieveTextResponse(argumentType) ?: retrieveValidTextResponse(argumentType, prompt)
    }

    private suspend fun <T> retrieveTextResponse(argumentType: ArgumentType<T>) = select<T?> {
        val timer = createTimer()

        exceptionBuffer.onReceive { timeoutException ->
            throw timeoutException
        }

        messageBuffer.onReceive { message ->
            userMessageIds.add(message.id)

            if (message.content == exitString)
                throw ExitException()

            timer?.cancel()

            when (val result = parseResponse(argumentType, message)) {
                is Success<T> -> result.result
                is Error<T> -> {
                    respond(result.error)
                    null
                }
            }
        }
    }

    @OptIn(KordPreview::class)
    private fun <T> retrieveValidInteractionResponse(buttons: Map<String, T>): T = runBlocking {
        retrieveInteractionResponse(buttons) ?: retrieveValidInteractionResponse(buttons)
    }

    @OptIn(KordPreview::class)
    private suspend fun <T> retrieveInteractionResponse(buttons: Map<String, T>) = select<T?> {
        val timer = createTimer()

        exceptionBuffer.onReceive { timeoutException ->
            throw timeoutException
        }

        messageBuffer.onReceive { message ->
            if (message.content == exitString) {
                timer?.cancel()
                throw ExitException()
            }
            else
                null
        }

        interactionBuffer.onReceive { interaction ->
            if (interaction.message?.id != previousBotMessageId)
                return@onReceive null

            timer?.cancel()
            interaction.acknowledgeEphemeralDeferredMessageUpdate()
            buttons[interaction.componentId]
        }
    }

    private suspend fun <T> parseResponse(argumentType: ArgumentType<T>, message: Message): ArgumentResult<T> {
        val rawInputs = RawInputs(message.content, "", 0, message.content.split(" "))
        val commandEvent = CommandEvent<TypeContainer>(rawInputs, discord, message, message.author!!, message.channel.asChannel(), message.getGuildOrNull())
        return argumentType.convert(message.content, commandEvent.rawInputs.commandArgs, commandEvent)
    }

    private fun createTimer() =
        timeout.takeIf { it > 0 }?.let { time ->
            Timer("Timeout", false).schedule(time) {
                runBlocking { exceptionBuffer.send(TimeoutException()) }
            }
        }
}

internal class TimeoutException : Exception("Prompt not answered in time.")
internal class ExitException : Exception("Conversation exited early.")
internal class DmException : Exception("Message failed to deliver.")
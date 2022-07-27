package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.Responder
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.dsl.uuid
import java.util.*
import kotlin.concurrent.schedule

/** @suppress DSL backing
 *
 * @param discord The discord instance.
 * @param user The user that the conversation is happening with.
 * @param channel The channel that the conversation is happening in.
 * @param exitString A String entered by the user to exit the conversation.
 */
public data class ConversationBuilder(val discord: Discord,
                                      val user: User,
                                      override val channel: MessageChannel,
                                      private val exitString: String? = null,
                                      private val timeout: Long) : Responder {
    private val messageBuffer = Channel<Message>()

    private val interactionBuffer = Channel<ComponentInteraction>()

    private val exceptionBuffer = Channel<TimeoutException>()

    /**
     * All ID's of messages sent by the bot in this conversation.
     */
    val botMessageIds: MutableList<Snowflake> = mutableListOf()

    /**
     * All ID's of messages sent by the user in this conversation.
     */
    val userMessageIds: MutableList<Snowflake> = mutableListOf()

    /**
     * The ID of the most recent message sent by the bot in this conversation.
     */
    val previousBotMessageId: Snowflake
        get() = botMessageIds.last()

    /**
     * The ID of the most recent message sent by the user in this conversation.
     */
    val previousUserMessageId: Snowflake
        get() = userMessageIds.last()

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
    public suspend fun <T> promptUntil(argument: Argument<*, T>, prompt: String, error: String, isValid: (T) -> Boolean): T {
        var value: T = prompt(argument, prompt)

        while (!isValid.invoke(value)) {
            channel.createMessage(error).also { it.let { botMessageIds.add(it.id) } }
            value = prompt(argument, prompt)
        }

        return value
    }

    /**
     * Prompt the user with text and/or embed.
     *
     * @param argument The [Argument] that the prompt expects in response.
     * @param text A String sent as part of the prompt.
     * @param embed The embed sent as part of the prompt.
     */
    @Throws(DmException::class, TimeoutException::class)
    public suspend fun <I, O> prompt(argument: Argument<I, O>, text: String = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): O {
        require(!argument.isOptional()) { "Conversation arguments cannot be optional" }

        val message = channel.createMessage {
            content = text.takeIf { it.isNotBlank() }

            if (embed != null) {
                val builder = EmbedBuilder()
                embed.invoke(builder)
                embeds.add(builder)
            }
        }

        botMessageIds.add(message.id)

        return retrieveValidTextResponse(argument)
    }

    /**
     * Prompt the user with an embed and the provided buttons.
     * Requires a call to both [ButtonPromptBuilder.embed] and [ButtonPromptBuilder.buttons].
     *
     * @param prompt The [builder][ButtonPromptBuilder]
     */
    @Throws(DmException::class, TimeoutException::class)
    public suspend fun <T> promptButton(prompt: suspend ButtonPromptBuilder<T>.() -> Unit): T {
        val builder = ButtonPromptBuilder<T>()
        prompt.invoke(builder)
        val message = builder.create(channel)

        botMessageIds.add(message.id)

        return retrieveValidInteractionResponse(builder.valueMap)
    }

    /**
     * Prompt the user with a select menu.
     *
     * @param options The options that can be selected by the user
     * @param embed The embed sent as part of the prompt.
     */
    @Throws(DmException::class, TimeoutException::class)
    public suspend fun promptSelect(vararg options: String, embed: suspend EmbedBuilder.() -> Unit): String {
        val message = channel.createMessage {
            val builder = EmbedBuilder()
            embed.invoke(builder)
            embeds.add(builder)

            actionRow {
                selectMenu(uuid()) {
                    options.forEach {
                        option(it, it)
                    }
                }
            }
        }

        botMessageIds.add(message.id)

        return retrieveValidInteractionResponse(options.associateWith { it })
    }

    private fun <T> retrieveValidTextResponse(argument: Argument<*, T>): T = runBlocking {
        retrieveTextResponse(argument) ?: retrieveValidTextResponse(argument)
    }

    private suspend fun <T> retrieveTextResponse(argument: Argument<*, T>) = select<T?> {
        val timer = createTimer()

        exceptionBuffer.onReceive { timeoutException ->
            throw timeoutException
        }

        messageBuffer.onReceive { message ->
            userMessageIds.add(message.id)

            if (message.content == exitString)
                throw ExitException()

            timer?.cancel()

            when (val result = parseResponse(argument, message)) {
                is Success<T> -> result.result
                is Error<T> -> {
                    respond(result.error)
                    null
                }
            }
        }
    }

    private fun <T> retrieveValidInteractionResponse(buttons: Map<String, T>): T = runBlocking {
        retrieveInteractionResponse(buttons) ?: retrieveValidInteractionResponse(buttons)
    }

    private suspend fun <T> retrieveInteractionResponse(buttons: Map<String, T>) = select<T?> {
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
            if (interaction.message.id != previousBotMessageId)
                return@onReceive null

            timer?.cancel()
            interaction.deferEphemeralMessageUpdate()

            if (interaction is SelectMenuInteraction)
                interaction.values.first() as T
            else
                buttons[interaction.componentId]
        }
    }

    private suspend fun <I, O> parseResponse(argument: Argument<I, O>, message: Message): Result<O> {
        val context = DiscordContext(discord, message, message.author!!, message.channel.asChannel(), message.getGuildOrNull())
        val parseResult = argument.parse(message.content.split(" ").toMutableList(), discord)

        return if (parseResult != null)
            argument.transform(parseResult, context)
        else
            Error(internalLocale.invalidFormat)
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
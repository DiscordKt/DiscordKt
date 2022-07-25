package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Result
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.Responder
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.dsl.uuid
import java.util.*
import kotlin.concurrent.schedule

public abstract class ConversationBuilder(
    public val discord: Discord,
    public val user: User,
    override val channel: MessageChannel,
    private val exitString: String? = null,
    private val timeout: Long,
) : Responder {
    private val messageBuffer = Channel<Message>()

    private val interactionBuffer = Channel<ComponentInteraction>()

    private val exceptionBuffer = Channel<TimeoutException>()

    public val responders: MutableList<MessageResponder> = mutableListOf()

    /**
     * All ID's of messages sent by the user in this conversation.
     */
    public val userMessageIds: MutableList<Snowflake> = mutableListOf()

    /**
     * The ID of the most recent message sent by the user in this conversation.
     */
    public val previousUserMessageId: Snowflake
        get() = userMessageIds.last()

    internal suspend fun acceptMessage(message: Message) = messageBuffer.send(message)

    internal suspend fun acceptInteraction(interaction: ComponentInteraction) = interactionBuffer.send(interaction)

    public abstract suspend fun <I, O> prompt(argument: Argument<I, O>, text: String = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): O
    public abstract suspend fun <T> promptUntil(argument: Argument<*, T>, prompt: String, error: String, isValid: (T) -> Boolean): T
    public abstract suspend fun <T> promptButton(prompt: suspend ButtonPromptBuilder<T>.() -> Unit): T
    public abstract suspend fun promptSelect(vararg options: String, embed: suspend EmbedBuilder.() -> Unit): String

    protected suspend fun createSelectMessage(options: Array<out String>, embed: suspend EmbedBuilder.() -> Unit, builder: MessageCreateBuilder) {
        with(builder) {
            val embedBuilder = EmbedBuilder()
            embed.invoke(embedBuilder)
            embeds.add(embedBuilder)

            actionRow {
                selectMenu(uuid()) {
                    options.forEach {
                        option(it, it)
                    }
                }
            }
        }
    }

    protected fun <T> retrieveValidTextResponse(argument: Argument<*, T>): T = runBlocking {
        retrieveTextResponse(argument) ?: retrieveValidTextResponse(argument)
    }

    private suspend fun <T> retrieveTextResponse(argument: Argument<*, T>): T? = select<T?> {
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

    protected fun <T> retrieveValidInteractionResponse(buttons: Map<String, T>): T = runBlocking {
        retrieveInteractionResponse(buttons) ?: retrieveValidInteractionResponse(buttons)
    }

    protected abstract suspend fun interactionIsOnLastBotMessage(interaction: ComponentInteraction): Boolean

    private suspend fun <T> retrieveInteractionResponse(buttons: Map<String, T>): T? = select<T?> {
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
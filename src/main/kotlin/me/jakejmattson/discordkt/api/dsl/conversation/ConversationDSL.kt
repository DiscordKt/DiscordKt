@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.conversation

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.api.services.ConversationResult
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.utils.Responder

private class ExitException : Exception("Conversation exited early.")
private class DmException : Exception("Message failed to deliver.")

/** @suppress DSL backing */
data class ConversationStateContainer(override val discord: Discord,
                                      val user: User,
                                      override val channel: MessageChannel,
                                      var exitString: String? = null) : Responder {

    private val messageBuffer = Channel<Message>()
    private val reactionBuffer = Channel<ReactionAddEvent>()

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
    internal suspend fun acceptReaction(reaction: ReactionAddEvent) = reactionBuffer.send(reaction)

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
            channel.createMessage(error).also { botMessageIds.add(it.id) }
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
    @Throws(DmException::class)
    fun <T> promptMessage(argumentType: ArgumentType<T>, prompt: String): T {
        require(!argumentType.isOptional) { "Conversation arguments cannot be optional" }
        return retrieveValidTextResponse(argumentType, prompt)
    }

    /**
     * Prompt the user with an embed. Re-prompt until the response converts correctly.
     *
     * @param argumentType The [ArgumentType] that the prompt expects in response.
     * @param prompt The embed sent to the user as a prompt for information.
     */
    @Throws(DmException::class)
    suspend fun <T> promptEmbed(argumentType: ArgumentType<T>, prompt: EmbedBuilder.() -> Unit): T {
        require(!argumentType.isOptional) { "Conversation arguments cannot be optional" }

        val message = channel.createEmbed {
            prompt.invoke(this)
        }

        botMessageIds.add(message.id)

        return retrieveValidTextResponse(argumentType, null)
    }

    /**
     * Prompt the user with an embed and the provided reactions.
     *
     * @param reactionMap A map of reactions that will be added to the embed and their values.
     * @param prompt The embed sent to the user as a prompt for information.
     */
    @Throws(DmException::class)
    suspend fun <T> promptReaction(reactionMap: Map<ReactionEmoji, T>, prompt: EmbedBuilder.() -> Unit): T {
        val message = channel.createEmbed {
            prompt.invoke(this)
        }

        botMessageIds.add(message.id)

        reactionMap.forEach {
            message.addReaction(it.key)
        }

        return retrieveValidReactionResponse(reactionMap)
    }

    private fun <T> retrieveValidTextResponse(argumentType: ArgumentType<*>, prompt: String?): T = runBlocking {
        prompt?.let { channel.createMessage(it) }?.also { botMessageIds.add(it.id) }
        retrieveTextResponse(argumentType) ?: retrieveValidTextResponse(argumentType, prompt)
    }

    private fun <T> retrieveValidReactionResponse(reactions: Map<ReactionEmoji, T>): T = runBlocking {
        retrieveReactionResponse(reactions) ?: retrieveValidReactionResponse(reactions)
    }

    private suspend fun <T> retrieveTextResponse(argumentType: ArgumentType<*>) = select<T?> {
        messageBuffer.onReceive { input ->
            userMessageIds.add(input.id)

            if (input.content == exitString)
                throw ExitException()

            when (val result = parseResponse(argumentType, input)) {
                is Success<*> -> result.result as T
                is Error<*> -> {
                    respond(result.error)
                    null
                }
            }
        }
    }

    private suspend fun <T> retrieveReactionResponse(reactions: Map<ReactionEmoji, T>) = select<T?> {
        reactionBuffer.onReceive { input ->
            if (input.messageId != previousBotMessageId)
                return@onReceive null

            val emoji = input.emoji

            reactions[emoji]
        }
    }

    private suspend fun parseResponse(argumentType: ArgumentType<*>, message: Message): ArgumentResult<*> {
        val rawInputs = RawInputs(message.content, "", message.content.split(" "), 0)
        val commandEvent = CommandEvent<Nothing>(rawInputs, DiscordContext(discord, message, guild = message.getGuildOrNull()))
        return argumentType.convert(message.content, commandEvent.rawInputs.commandArgs, commandEvent)
    }
}

/** @suppress Intermediate return type */
class ConversationBuilder(private val exitString: String?, private val block: suspend ConversationStateContainer.() -> Unit) {
    private lateinit var stateContainer: ConversationStateContainer

    @PublishedApi
    internal suspend fun start(conversationStateContainer: ConversationStateContainer, onEnd: () -> Unit): ConversationResult {
        conversationStateContainer.exitString = exitString
        stateContainer = conversationStateContainer

        return try {
            block.invoke(conversationStateContainer)
            ConversationResult.COMPLETE
        } catch (e: ExitException) {
            ConversationResult.EXITED
        } catch (e: DmException) {
            ConversationResult.CANNOT_DM
        } finally {
            onEnd.invoke()
        }
    }

    @PublishedApi
    internal suspend fun acceptMessage(message: Message) = stateContainer.acceptMessage(message)

    @PublishedApi
    internal suspend fun acceptReaction(reaction: ReactionAddEvent) = stateContainer.acceptReaction(reaction)
}

/**
 * This block builds a conversation.
 *
 * @param exitString If this String is entered by the user, the conversation is exited.
 */
@BuilderDSL
fun conversation(exitString: String? = null, block: suspend ConversationStateContainer.() -> Unit) = ConversationBuilder(exitString, block)
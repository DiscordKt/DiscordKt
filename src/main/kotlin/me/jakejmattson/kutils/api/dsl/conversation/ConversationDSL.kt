@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.conversation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.annotations.BuilderDSL
import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.dsl.embed.*
import me.jakejmattson.kutils.api.services.ConversationResult
import me.jakejmattson.kutils.internal.utils.Responder
import net.dv8tion.jda.api.entities.*

private class ExitException : Exception("Conversation exited early.")
private class DmException : Exception("Message failed to deliver.")

/** @suppress DSL backing */
data class ConversationStateContainer(val discord: Discord,
                                      val user: User,
                                      override val channel: MessageChannel,
                                      var exitString: String? = null) : Responder {

    private val messageBuffer = Channel<Message>()
    private val reactionBuffer = Channel<MessageReaction>()

    /**
     * All ID's of messages sent by the bot in this conversation.
     */
    val botMessageIds = mutableListOf<String>()

    /**
     * All ID's of messages sent by the user in this conversation.
     */
    val userMessageIds = mutableListOf<String>()

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
    internal suspend fun acceptReaction(reaction: MessageReaction) = reactionBuffer.send(reaction)

    /**
     * Prompt the user with a String. Re-prompt until the response converts correctly. Then apply a custom predicate as an additional check.
     *
     * @param argumentType The [ArgumentType] that the prompt expects in response.
     * @param prompt The string message sent to the user as a prompt for information.
     * @param error The error String to send when the input fails the custom check.
     * @param isValid A predicate to determine whether or not the input is accepted.
     */
    @Throws(DmException::class)
    fun <T> promptUntil(argumentType: ArgumentType<T>, prompt: String, error: String, isValid: (T) -> Boolean): T {
        var value: T = promptMessage(argumentType, prompt)

        while (!isValid.invoke(value)) {
            sendPrompt(error)
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
    fun <T> promptEmbed(argumentType: ArgumentType<T>, prompt: EmbedDSL.() -> Unit): T {
        require(!argumentType.isOptional) { "Conversation arguments cannot be optional" }
        return retrieveValidTextResponse(argumentType, embed(prompt))
    }

    /**
     * Prompt the user with an embed and the provided reactions.
     *
     * @param reactionMap A map of reactions that will be added to the embed and their values.
     * @param prompt The embed sent to the user as a prompt for information.
     */
    @Throws(DmException::class)
    fun <T> promptReaction(reactionMap: Map<String, T>, prompt: EmbedDSL.() -> Unit): T {
        channel.sendMessage(embed(prompt)).queue { message ->
            botMessageIds.add(message.id)

            reactionMap.forEach {
                message.addReaction(it.key).queue()
            }
        }

        return retrieveValidReactionResponse(reactionMap)
    }

    private fun <T> retrieveValidTextResponse(argumentType: ArgumentType<*>, prompt: Any): T = runBlocking<T> {
        sendPrompt(prompt)
        retrieveTextResponse(argumentType) ?: retrieveValidTextResponse(argumentType, prompt)
    }

    private fun <T> retrieveValidReactionResponse(reactions: Map<String, T>): T = runBlocking<T> {
        retrieveReactionResponse(reactions) ?: retrieveValidReactionResponse(reactions)
    }

    private suspend fun <T> retrieveTextResponse(argumentType: ArgumentType<*>) = select<T?> {
        messageBuffer.onReceive { input ->
            userMessageIds.add(input.id)

            if (input.contentStripped == exitString)
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

    private suspend fun <T> retrieveReactionResponse(reactions: Map<String, T>) = select<T?> {
        reactionBuffer.onReceive { input ->
            if (input.messageId != previousBotMessageId)
                return@onReceive null

            val emoji = input.reactionEmote.emoji

            reactions[emoji]
        }
    }

    private fun parseResponse(argumentType: ArgumentType<*>, message: Message): ArgumentResult<*> {
        val rawInputs = RawInputs(message.contentRaw, "", message.contentStripped.split(" "), 0)
        val commandEvent = CommandEvent<Nothing>(rawInputs, CommandsContainer(), DiscordContext(discord, message))
        return argumentType.convert(message.contentStripped, commandEvent.rawInputs.commandArgs, commandEvent)
    }

    private fun sendPrompt(prompt: Any) =
        when (prompt) {
            is String -> channel.sendMessage(prompt).queue { botMessageIds.add(it.id) }
            is MessageEmbed -> channel.sendMessage(prompt).queue { botMessageIds.add(it.id) }
            else -> Unit
        }
}

/** @suppress Intermediate return type */
class ConversationBuilder(private val exitString: String?, private val block: (ConversationStateContainer) -> Unit) {
    private lateinit var stateContainer: ConversationStateContainer

    @PublishedApi
    internal fun start(conversationStateContainer: ConversationStateContainer, onEnd: () -> Unit): ConversationResult {
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
    internal suspend fun acceptReaction(reaction: MessageReaction) = stateContainer.acceptReaction(reaction)
}

/**
 * This block builds a conversation.
 *
 * @param exitString If this String is entered by the user, the conversation is exited.
 */
@BuilderDSL
fun conversation(exitString: String? = null, block: ConversationStateContainer.() -> Unit) = ConversationBuilder(exitString, block)
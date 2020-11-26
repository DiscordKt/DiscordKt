@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.*
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL

private class ExitException : Exception("Conversation exited early.")
private class DmException : Exception("Message failed to deliver.")

/**
 * An enum representing possible ways that a conversation can end.
 */
enum class ConversationResult {
    /** The target user cannot be reached - a bot or no shared guild. */
    INVALID_USER,

    /** The target user has the bot blocked or has DMs off. */
    CANNOT_DM,

    /** The target user already has a conversation. */
    HAS_CONVERSATION,

    /** The conversation has completed successfully. */
    COMPLETE,

    /** The conversation was exited by the user. */
    EXITED
}

/**
 * This block builds a conversation.
 *
 * @param exitString A String entered by the user to exit the conversation.
 */
@BuilderDSL
fun conversation(exitString: String? = null, block: suspend ConversationBuilder.() -> Unit) = Conversation(exitString, block)

/**
 * A class that represent a conversation.
 *
 * @param exitString A String entered by the user to exit the conversation.
 */
class Conversation(var exitString: String? = null, private val block: suspend ConversationBuilder.() -> Unit) {
    /**
     * Start a conversation with someone in their private messages.
     *
     * @param user The user to start a conversation with.
     *
     * @return The result of the conversation indicated by an enum.
     * @sample ConversationResult
     */
    suspend inline fun startPrivately(discord: Discord, user: User): ConversationResult {
        if (user.isBot)
            return ConversationResult.INVALID_USER

        val channel = user.getDmChannel()

        if (Conversations.hasConversation(user, channel))
            return ConversationResult.HAS_CONVERSATION

        val state = ConversationBuilder(discord, user, channel, exitString)

        return start(state)
    }

    /**
     * Start a conversation with someone in a public channel.
     *
     * @param user The user to start a conversation with.
     * @param channel The guild channel to start the conversation in.
     *
     * @return The result of the conversation indicated by an enum.
     * @sample ConversationResult
     */
    suspend inline fun startPublicly(discord: Discord, user: User, channel: MessageChannel): ConversationResult {
        if (user.isBot)
            return ConversationResult.INVALID_USER

        if (Conversations.hasConversation(user, channel))
            return ConversationResult.HAS_CONVERSATION

        val state = ConversationBuilder(discord, user, channel, exitString)

        return start(state)
    }

    private lateinit var builder: ConversationBuilder

    @PublishedApi
    internal suspend fun start(conversationBuilder: ConversationBuilder): ConversationResult {
        val (_, user, channel) = conversationBuilder
        builder = conversationBuilder

        return try {
            Conversations.start(user, channel, this)
            block.invoke(conversationBuilder)
            ConversationResult.COMPLETE
        } catch (e: ExitException) {
            ConversationResult.EXITED
        } catch (e: DmException) {
            ConversationResult.CANNOT_DM
        } finally {
            Conversations.end(user, channel)
        }
    }

    @PublishedApi
    internal suspend fun acceptMessage(message: Message) = builder.acceptMessage(message)

    @PublishedApi
    internal suspend fun acceptReaction(reaction: ReactionAddEvent) = builder.acceptReaction(reaction)
}

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
                               private val exitString: String? = null) : Responder {

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
    suspend fun <T> promptEmbed(argumentType: ArgumentType<T>, prompt: suspend EmbedBuilder.() -> Unit): T {
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
    suspend fun <T> promptReaction(reactionMap: Map<ReactionEmoji, T>, prompt: suspend EmbedBuilder.() -> Unit): T {
        val message = channel.createEmbed {
            prompt.invoke(this)
        }

        botMessageIds.add(message.id)

        reactionMap.forEach {
            message.addReaction(it.key)
        }

        return retrieveValidReactionResponse(reactionMap)
    }

    private fun <T> retrieveValidTextResponse(argumentType: ArgumentType<T>, prompt: String?): T = runBlocking {
        prompt?.let { channel.createMessage(it) }?.also { botMessageIds.add(it.id) }
        retrieveTextResponse(argumentType) ?: retrieveValidTextResponse(argumentType, prompt)
    }

    private fun <T> retrieveValidReactionResponse(reactions: Map<ReactionEmoji, T>): T = runBlocking {
        retrieveReactionResponse(reactions) ?: retrieveValidReactionResponse(reactions)
    }

    private suspend fun <T> retrieveTextResponse(argumentType: ArgumentType<T>) = select<T?> {
        messageBuffer.onReceive { input ->
            userMessageIds.add(input.id)

            if (input.content == exitString)
                throw ExitException()

            when (val result = parseResponse(argumentType, input)) {
                is Success<T> -> result.result
                is Error<T> -> {
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

            reactions[input.emoji]
        }
    }

    private suspend fun <T> parseResponse(argumentType: ArgumentType<T>, message: Message): ArgumentResult<T> {
        val rawInputs = RawInputs(message.content, "", message.content.split(" "), 0)
        val commandEvent = CommandEvent<TypeContainer>(rawInputs, discord, message, message.author!!, message.channel.asChannel(), message.getGuildOrNull())
        return argumentType.convert(message.content, commandEvent.rawInputs.commandArgs, commandEvent)
    }
}

/**
 * Object to keep tracking of running conversations.
 */
object Conversations {
    private data class ConversationLocation(val userId: Snowflake, val channelId: Snowflake)

    private val activeConversations = mutableMapOf<ConversationLocation, Conversation>()

    @PublishedApi
    internal fun start(user: User, channel: MessageChannel, conversation: Conversation) {
        activeConversations[ConversationLocation(user.id, channel.id)] = conversation
    }

    @PublishedApi
    internal fun end(user: User, channel: MessageChannel) {
        activeConversations.remove(ConversationLocation(user.id, channel.id))
    }

    /**
     * Get a running conversation by its context, if it exists.
     */
    private fun getConversation(user: User, channel: MessageChannelBehavior) = activeConversations[ConversationLocation(user.id, channel.id)]

    /**
     * Whether or not a conversation with the given context already exists.
     */
    fun hasConversation(user: User, channel: MessageChannelBehavior) = getConversation(user, channel) != null

    internal fun handleMessage(message: Message) {
        runBlocking {
            getConversation(message.author!!, message.channel)?.acceptMessage(message)
        }
    }

    internal fun handleReaction(author: User, channel: MessageChannelBehavior, reaction: ReactionAddEvent) {
        runBlocking {
            getConversation(author, channel)?.acceptReaction(reaction)
        }
    }
}
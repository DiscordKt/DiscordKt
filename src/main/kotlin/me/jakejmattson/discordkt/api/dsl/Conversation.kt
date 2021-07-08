@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.interaction.embed
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.TypeContainer
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
 * A reaction prompted for inside a conversation
 *
 * @property emoji The actual Discord emoji to be reacted with.
 * @property description The String description used in the prompt.
 * @property value The value returned when this emoji is selected.
 */
data class PromptedReaction<T>(val emoji: DiscordEmoji, val description: String, val value: T)

class Messenger @OptIn(KordPreview::class) constructor(private val either: Either<MessageChannel, EphemeralInteractionResponseBehavior>) {
    @OptIn(DelicateCoroutinesApi::class, KordPreview::class)
    suspend fun createMessage(message: String) =
        either.map(
            { it.createMessage(message).id },
            { it.followUpEphemeral { content = message }; null }
        )

    @OptIn(DelicateCoroutinesApi::class, KordPreview::class)
    suspend fun createEmbed(builder: suspend EmbedBuilder.() -> Unit) =
        either.map(
            { it.createEmbed { builder.invoke(this) } },
            { it.followUpEphemeral { embed { builder.invoke(this) } }; null }
        )
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

        val messenger = Messenger(Left(channel))
        val state = ConversationBuilder(discord, user, channel, messenger, exitString)

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

        val messenger = Messenger(Left(channel))
        val state = ConversationBuilder(discord, user, channel, messenger, exitString)

        return start(state)
    }

    /**
     * Start a conversation with someone via ephemeral messages.
     *
     * @param interaction The [ComponentInteraction] to use as a starting point.
     *
     * @return The result of the conversation indicated by an enum.
     * @sample ConversationResult
     */
    @OptIn(KordPreview::class)
    suspend inline fun startEphemeral(discord: Discord, interaction: ComponentInteraction): ConversationResult {
        val user = interaction.user.asUser()
        val channel = discord.kord.getChannelOf<MessageChannel>(interaction.channelId)!!

        if (Conversations.hasConversation(user, channel))
            return ConversationResult.HAS_CONVERSATION

        val ephemeralResponse = interaction.acknowledgeEphemeralDeferredMessageUpdate()
        val messenger = Messenger(Right(ephemeralResponse))
        val state = ConversationBuilder(discord, user, channel, messenger, exitString)

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
                               private val messenger: Messenger,
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
            messenger.createMessage(error).also { it?.let { botMessageIds.add(it) } }
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
        require(argumentType !is OptionalArg<*>) { "Conversation arguments cannot be optional" }
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
        require(argumentType !is OptionalArg<*>) { "Conversation arguments cannot be optional" }

        val message = messenger.createEmbed {
            prompt.invoke(this)
        }

        if (message != null) {
            botMessageIds.add(message.id)
        }

        return retrieveValidTextResponse(argumentType, null)
    }

    /**
     * Prompt the user with an embed and the provided reactions.
     *
     * @param reactions Collection of [PromptedReaction] that will be added to the embed.
     * @param prompt The embed sent to the user as a prompt for information.
     */
    @Throws(DmException::class)
    suspend fun <T> promptReaction(vararg reactions: PromptedReaction<T>, prompt: suspend EmbedBuilder.() -> Unit): T {
        val message = messenger.createEmbed {
            prompt.invoke(this)

            field {
                name = "Options"
                value = reactions.joinToString("\n") { "${it.emoji.unicode} - ${it.description}" }
            }
        }

        if (message != null) {
            botMessageIds.add(message.id)
        }

        reactions.forEach {
            message?.addReaction(it.emoji.toReaction())
        }

        return retrieveValidReactionResponse(reactions.associate { it.emoji.toReaction() to it.value })
    }

    private fun <T> retrieveValidTextResponse(argumentType: ArgumentType<T>, prompt: String?): T = runBlocking {
        prompt?.let { messenger.createMessage(it) }?.also { botMessageIds.add(it) }
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
        val rawInputs = RawInputs(message.content, "", 0, message.content.split(" "))
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
    private fun getConversation(user: User, channel: MessageChannel) = activeConversations[ConversationLocation(user.id, channel.id)]

    /**
     * Whether or not a conversation with the given context already exists.
     */
    fun hasConversation(user: User, channel: MessageChannel) = getConversation(user, channel) != null

    internal fun handleMessage(message: Message) {
        runBlocking {
            getConversation(message.author!!, message.channel.asChannel())?.acceptMessage(message)
        }
    }

    internal fun handleReaction(author: User, channel: MessageChannel, reaction: ReactionAddEvent) {
        runBlocking {
            getConversation(author, channel)?.acceptReaction(reaction)
        }
    }
}
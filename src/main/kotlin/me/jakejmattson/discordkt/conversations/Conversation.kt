package me.jakejmattson.discordkt.conversations

import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.SlashCommandEvent
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL

/**
 * This block builds a conversation.
 *
 * @param exitString A String entered by the user to exit the conversation.
 */
@BuilderDSL
public fun conversation(exitString: String? = null, promptTimeout: Long = 0, block: suspend ConversationBuilder.() -> Unit): Conversation =
    Conversation(exitString, promptTimeout * 1000, block)

/**
 * A class that represent a conversation.
 *
 * @param exitString A String entered by the user to exit the conversation.
 * @param promptTimeout The amount of time (in seconds) before a prompt times out and is aborted.
 */
public class Conversation(public var exitString: String? = null, public var promptTimeout: Long, private val block: suspend ConversationBuilder.() -> Unit) {
    /**
     * Start a conversation with someone from a slash command.
     *
     * @param user The user to start a conversation with.
     * @param event The event used to start the conversation.
     *
     * @return The result of the conversation indicated by an enum.
     * @sample me.jakejmattson.discordkt.conversations.ConversationResult
     */
    public suspend inline fun <T : TypeContainer> startSlashResponse(discord: Discord, user: User, event: SlashCommandEvent<T>): ConversationResult {
        if (user.isBot)
            return ConversationResult.INVALID_USER

        if (Conversations.hasConversation(user, event.channel))
            return ConversationResult.HAS_CONVERSATION

        val state = SlashConversationBuilder(discord, user, event, exitString, promptTimeout)

        return start(state)
    }

    private lateinit var builder: ConversationBuilder

    @PublishedApi
    internal suspend fun start(conversationBuilder: ConversationBuilder): ConversationResult {
        val user = conversationBuilder.user
        val channel = conversationBuilder.channel
        builder = conversationBuilder

        return try {
            Conversations.start(user, channel, this)
            block.invoke(conversationBuilder)
            ConversationResult.COMPLETE
        } catch (e: TimeoutException) {
            ConversationResult.TIMED_OUT
        } catch (e: ExitException) {
            ConversationResult.EXITED
        } catch (e: DmException) {
            ConversationResult.CANNOT_DM
        } finally {
            Conversations.end(user, channel)
        }
    }

    @PublishedApi
    internal suspend fun acceptMessage(message: Message): Unit = builder.acceptMessage(message)

    @PublishedApi
    internal suspend fun acceptInteraction(interaction: ComponentInteraction): Unit = builder.acceptInteraction(interaction)
}

/**
 * An enum representing possible ways that a conversation can end.
 */
public enum class ConversationResult {
    /** The target user cannot be reached - a bot or no shared guild. */
    INVALID_USER,

    /** The target user has the bot blocked or has DMs off. */
    CANNOT_DM,

    /** The target user already has a conversation. */
    HAS_CONVERSATION,

    /** The conversation prompt timed out with no fallback. */
    TIMED_OUT,

    /** The conversation has completed successfully. */
    COMPLETE,

    /** The conversation was exited by the user. */
    EXITED
}
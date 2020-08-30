@file:Suppress("unused")

package me.jakejmattson.discordkt.api.services

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.api.services.ConversationResult.*
import me.jakejmattson.discordkt.internal.utils.*
import java.lang.reflect.Method

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

@PublishedApi
internal data class ConversationContext(val userId: Snowflake, val channelId: Snowflake)

/**
 * A service to keep track of registered and ongoing conversations, as well as start new ones.
 *
 * @param discord An instance of the [Discord] object used to create the ConversationStateContainer.
 */
class ConversationService(val discord: Discord) {
    @PublishedApi
    internal val availableConversations = mutableMapOf<Class<out Conversation>, Pair<Conversation, Method>>()

    @PublishedApi
    internal val activeConversations = mutableMapOf<ConversationContext, ConversationBuilder>()

    internal fun registerConversations(path: String): Int {
        val startFunctions = ReflectionUtils.detectMethodsWith<Conversation.Start>(path)

        ReflectionUtils.detectSubtypesOf<Conversation>(path)
            .forEach { conversationClass ->
                val relevantStartFunctions = startFunctions.filter { it.declaringClass == conversationClass }
                val conversationName = conversationClass.name.substringAfterLast(".")

                val starter = when (relevantStartFunctions.size) {
                    0 -> {
                        InternalLogger.error("$conversationName has no method annotated with @Start. It cannot be registered.")
                        return@forEach
                    }
                    else -> {
                        if (relevantStartFunctions.size != 1)
                            InternalLogger.error("$conversationName has multiple methods annotated with @Start. Searching for best fit.")

                        relevantStartFunctions.firstOrNull { it.returnType == ConversationBuilder::class.java }
                    }
                }
                    ?: return@forEach InternalLogger.error("$conversationName @Start function does not build a conversation.")

                val instance = diService.invokeConstructor(conversationClass)
                availableConversations[conversationClass] = instance to starter
            }

        return availableConversations.size
    }

    private fun getConversation(user: User, channel: MessageChannelBehavior) = activeConversations[ConversationContext(user.id, channel.id)]

    /**
     * Whether or not a conversation with the given context already exists.
     */
    fun hasConversation(user: User, channel: MessageChannelBehavior) = getConversation(user, channel) != null

    @PublishedApi
    internal suspend inline fun <reified T : Conversation> startConversation(stateContainer: ConversationStateContainer, vararg arguments: Any): ConversationResult {
        val (_, user, channel) = stateContainer
        val context = ConversationContext(user.id, channel.id)

        val (instance, function) = availableConversations[T::class.java]!!
        val conversation = function.invoke(instance, *arguments) as ConversationBuilder

        activeConversations[context] = conversation

        return conversation.start(stateContainer) {
            activeConversations.remove(context)
        }
    }

    /**
     * Start a conversation with someone in their private messages.
     *
     * @param user The user to start a conversation with.
     * @param arguments The data you'd like to inject into the conversation.
     *
     * @return The result of the conversation indicated by an enum.
     * @sample ConversationResult
     */
    suspend inline fun <reified T : Conversation> startPrivateConversation(user: User, vararg arguments: Any): ConversationResult {
        if (user.isBot == true)
            return INVALID_USER

        val channel = user.getDmChannel()

        if (hasConversation(user, channel))
            return HAS_CONVERSATION

        val state = ConversationStateContainer(discord, user, channel)

        return startConversation<T>(state, *arguments)
    }

    /**
     * Start a conversation with someone in a public channel.
     *
     * @param user The user to start a conversation with.
     * @param channel The guild channel to start the conversation in.
     * @param arguments The data you'd like to inject into the conversation.
     *
     * @return The result of the conversation indicated by an enum.
     * @sample ConversationResult
     */
    suspend inline fun <reified T : Conversation> startPublicConversation(user: User, channel: MessageChannel, vararg arguments: Any): ConversationResult {
        if (user.isBot == true)
            return INVALID_USER

        if (hasConversation(user, channel))
            return HAS_CONVERSATION

        val state = ConversationStateContainer(discord, user, channel)

        return startConversation<T>(state, *arguments)
    }

    internal fun handleMessage(message: Message) {
        runBlocking {
            val conversation = getConversation(message.author!!, message.channel) ?: return@runBlocking
            conversation.acceptMessage(message)
        }
    }

    internal fun handleReaction(author: User, channel: MessageChannelBehavior, reaction: ReactionAddEvent) {
        runBlocking {
            val conversation = getConversation(author, channel) ?: return@runBlocking
            conversation.acceptReaction(reaction)
        }
    }
}

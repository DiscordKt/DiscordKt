@file:Suppress("unused")

package me.jakejmattson.kutils.api.services

import kotlinx.coroutines.runBlocking
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.dsl.conversation.*
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import me.jakejmattson.kutils.api.services.ConversationResult.*
import me.jakejmattson.kutils.internal.utils.*
import net.dv8tion.jda.api.entities.*
import java.lang.reflect.Method

/**
 * @property INVALID_USER The target user cannot be reached - a bot or no shared guild.
 * @property CANNOT_DM The target user has the bot blocked or has DMs off.
 * @property HAS_CONVO The target user already has a conversation.
 * @property COMPLETE The conversation has completed successfully.
 * @property EXITED The conversation was exited by the user.
 */
enum class ConversationResult {
    INVALID_USER,
    CANNOT_DM,
    HAS_CONVO,
    COMPLETE,
    EXITED
}

data class ConversationContext(val userId: String, val channelId: String)

class ConversationService(val discord: Discord) {
    @PublishedApi
    internal val availableConversations = mutableMapOf<Class<out Conversation>, Pair<Conversation, Method>>()

    @PublishedApi
    internal val activeConversations = mutableMapOf<ConversationContext, ConversationBuilder>()

    internal fun registerConversations(path: String) {
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

        InternalLogger.startup(availableConversations.size.pluralize("Conversation"))
    }

    private fun getConversation(user: User, channel: MessageChannel) = activeConversations[ConversationContext(user.id, channel.id)]
    fun hasConversation(user: User, channel: MessageChannel) = getConversation(user, channel) != null

    @PublishedApi
    internal inline fun <reified T : Conversation> startConversation(stateContainer: ConversationStateContainer, vararg arguments: Any): ConversationResult {
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
    inline fun <reified T : Conversation> startPrivateConversation(user: User, vararg arguments: Any): ConversationResult {
        if (user.mutualGuilds.isEmpty() || user.isBot)
            return INVALID_USER

        val channel = user.openPrivateChannel().complete()

        if (hasConversation(user, channel))
            return HAS_CONVO

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
    inline fun <reified T : Conversation> startPublicConversation(user: User, channel: MessageChannel, vararg arguments: Any): ConversationResult {
        if (user.mutualGuilds.isEmpty() || user.isBot)
            return INVALID_USER

        if (hasConversation(user, channel))
            return HAS_CONVO

        val state = ConversationStateContainer(discord, user, channel)

        return startConversation<T>(state, *arguments)
    }

    internal fun handleMessage(message: Message) {
        runBlocking {
            val conversation = getConversation(message.author, message.channel) ?: return@runBlocking
            conversation.acceptMessage(message)
        }
    }

    internal fun handleReaction(author: User, channel: MessageChannel, reaction: MessageReaction) {
        runBlocking {
            val conversation = getConversation(author, channel) ?: return@runBlocking
            conversation.acceptReaction(reaction)
        }
    }
}

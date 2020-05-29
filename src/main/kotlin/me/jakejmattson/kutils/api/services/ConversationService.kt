package me.jakejmattson.kutils.api.services

import kotlinx.coroutines.runBlocking
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.dsl.conversation.*
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import me.jakejmattson.kutils.internal.utils.*
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import java.lang.reflect.Method

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
        val startFunctions = Reflections(path, MethodAnnotationsScanner())
            .getMethodsAnnotatedWith(Conversation.Start::class.java)

        Reflections(path)
            .getSubTypesOf(Conversation::class.java)
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

                starter
                    ?: return@forEach InternalLogger.error("$conversationName @Start function does not build a conversation. It cannot be registered.")

                val instance = diService.invokeConstructor(conversationClass) as Conversation
                availableConversations[conversationClass as Class<out Conversation>] = instance to starter
            }

        println(availableConversations.size.pluralize("Conversation"))
    }

    private fun getConversation(user: User, channel: MessageChannel) = activeConversations[ConversationContext(user.id, channel.id)]
    fun hasConversation(user: User, channel: MessageChannel) = getConversation(user, channel) != null

    inline fun <reified T : Conversation> startConversation(stateContainer: ConversationStateContainer, vararg arguments: Any): ConversationResult {
        val (_, user, channel) = stateContainer
        val context = ConversationContext(user.id, channel.id)

        val (instance, function) = availableConversations[T::class.java]!!
        val conversation = function.invoke(instance, *arguments) as ConversationBuilder

        activeConversations[context] = conversation

        return conversation.start(stateContainer) {
            activeConversations.remove(context)
        }
    }

    inline fun <reified T : Conversation> startPrivateConversation(user: User, vararg arguments: Any): ConversationResult {
        if (user.mutualGuilds.isEmpty() || user.isBot)
            return ConversationResult.INVALID_USER

        val channel = user.openPrivateChannel().complete()

        if (hasConversation(user, channel))
            return ConversationResult.HAS_CONVO

        val state = ConversationStateContainer(discord, user, channel)

        return startConversation<T>(state, *arguments)
    }

    inline fun <reified T : Conversation> startPublicConversation(user: User, channel: MessageChannel, vararg arguments: Any): ConversationResult {
        if (user.mutualGuilds.isEmpty() || user.isBot)
            return ConversationResult.INVALID_USER

        if (hasConversation(user, channel))
            return ConversationResult.HAS_CONVO

        val state = ConversationStateContainer(discord, user, channel)

        return startConversation<T>(state, *arguments)
    }

    internal fun handleResponse(message: Message) {
        runBlocking {
            val conversation = getConversation(message.author, message.channel) ?: return@runBlocking
            conversation.acceptMessage(message)
        }
    }
}

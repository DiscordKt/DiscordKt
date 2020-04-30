package me.aberrantfox.kjdautils.internal.services

import kotlinx.coroutines.runBlocking
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.pluralize
import me.aberrantfox.kjdautils.internal.utils.InternalLogger
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import kotlin.reflect.jvm.kotlinFunction

class ConversationService(val discord: Discord) {
    @PublishedApi
    internal val availableConversations = mutableMapOf<Class<out Conversation>, Conversation>()

    @PublishedApi
    internal val activeConversations = mutableMapOf<String, ConversationBuilder>()

    fun getConversation(user: User) = activeConversations[user.id]
    fun hasConversation(user: User) = getConversation(user) != null

    fun registerConversations(path: String) {
        Reflections(path)
            .getSubTypesOf(Conversation::class.java)
            .forEach {
                val startFunctions = Reflections(it, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Conversation.Start::class.java)
                val conversationName = it.name

                when(startFunctions.size) {
                    0 -> {
                        InternalLogger.error("Conversation $conversationName has no method annotated with @Start")
                        return@forEach
                    }
                    1 -> { }
                    else -> {
                        InternalLogger.error("Conversation $conversationName has multiple methods annotated with @Start. Using first.")
                    }
                }

                availableConversations[it as Class<out Conversation>] = it.constructors.first().newInstance() as Conversation
            }

        println(availableConversations.size.pluralize("Conversation"))
    }

    inline fun <reified T : Conversation> startConversation(user: User, vararg arguments: Any): Boolean {
        if (user.isBot)
            return false

        if (hasConversation(user))
            return false

        val conversationClass = availableConversations[T::class.java]

        require(conversationClass != null) { "No conversation found: ${T::class}" }

        val starter = Reflections(T::class.java, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Conversation.Start::class.java).first()

        require(starter.returnType == ConversationBuilder::class.java) { "Conversation @Start function must build a conversation." }

        val conversation = starter.invoke(conversationClass, *arguments) as ConversationBuilder

        activeConversations[user.id] = conversation

        val state = ConversationStateContainer(user, discord)

        conversation.start(state) {
            activeConversations.remove(user.id)
        }

        return true
    }

    fun handleResponse(message: Message) {
        runBlocking {
            val conversation = getConversation(message.author) ?: return@runBlocking
            conversation.acceptMessage(message)
        }
    }
}

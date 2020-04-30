package me.aberrantfox.kjdautils.internal.services

import kotlinx.coroutines.runBlocking
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.pluralize
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections

class ConversationService(val discord: Discord) {
    @PublishedApi
    internal val availableConversations = mutableMapOf<Class<out ConversationBase>, ConversationBase>()

    @PublishedApi
    internal val activeConversations = mutableMapOf<String, ConversationBuilder>()

    fun getConversation(user: User) = activeConversations[user.id]
    fun hasConversation(user: User) = getConversation(user) != null

    fun registerConversations(path: String) {
        Reflections(path)
            .getSubTypesOf(ConversationBase::class.java)
            .forEach {
                availableConversations[it as Class<out ConversationBase>] = it.constructors.first().newInstance() as ConversationBase
            }

        println(availableConversations.size.pluralize("Conversation"))
    }

    inline fun <reified T : ConversationBase> startConversation(user: User, vararg arguments: Any): Boolean {
        if (user.isBot)
            return false

        if (hasConversation(user))
            return false

        val conversationClass = availableConversations[T::class.java]

        require(conversationClass != null) { "No conversation found: ${T::class}" }

        val conversation = conversationClass.conversation()

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

package me.aberrantfox.kjdautils.internal.services

import kotlinx.coroutines.runBlocking
import me.aberrantfox.kjdautils.api.annotation.Convo
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.pluralize
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

class ConversationService(private val discord: Discord, private val diService: DIService) {
    private val availableConversations = mutableListOf<Conversation>()
    private val activeConversations = mutableMapOf<String, Conversation>()

    private fun getConversation(user: User) = activeConversations[user.id]
    fun hasConversation(user: User) = getConversation(user) != null

    fun registerConversations(path: String) {
        val conversations = Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Convo::class.java)

        println(conversations.size.pluralize("Conversation"))

        conversations.forEach {
            availableConversations.add(diService.invokeReturningMethod(it) as Conversation)
        }
    }

    fun createConversation(user: User, guild: Guild, conversationName: String) {
        if (hasConversation(user))
            return

        if (!user.isBot) {
            val conversation = availableConversations.firstOrNull { it.name == conversationName }

            require(conversation != null) { "No conversation found with the name: $conversationName" }

            val state = ConversationStateContainer(user, guild, discord)
            activeConversations[user.id] = conversation
            conversation.start(state) {
                activeConversations.remove(user.id)
            }
        }
    }

    fun handleResponse(message: Message) {
        runBlocking {
            val conversation = getConversation(message.author) ?: return@runBlocking
            conversation.acceptMessage(message)
        }
    }
}

package me.aberrantfox.kjdautils.internal.services

import kotlinx.coroutines.runBlocking
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.di.DIService
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

class ConversationService(private val discord: Discord, private val diService: DIService) {
    private val availableConversations = mutableListOf<Conversation>()
    private val activeConversations = mutableMapOf<String, Conversation>()

    private fun getConversation(user: User) = activeConversations[user.id]

    fun registerConversations(path: String) {
        Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Convo::class.java).forEach {
            availableConversations.add(diService.invokeReturningMethod(it) as Conversation)
        }
    }

    fun createConversation(user: User, guild: Guild, conversationName: String) {
        if (getConversation(user) != null)
            return

        if (!user.isBot) {
            val conversation = availableConversations.firstOrNull { it.name == conversationName }

            require(conversation != null) { "No conversation found with the name: $conversationName" }

            val state = ConversationStateContainer(user, guild, discord)
            activeConversations[user.id] = conversation
            conversation.start(state)
        }
    }

    fun handleResponse(message: Message) {
        runBlocking {
            val conversation = getConversation(message.author) ?: return@runBlocking
            conversation.stateContainer.inputChannel.send(message)
        }
    }
}

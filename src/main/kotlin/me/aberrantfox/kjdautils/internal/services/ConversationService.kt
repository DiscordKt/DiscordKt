package me.aberrantfox.kjdautils.internal.services

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.CommandStruct
import me.aberrantfox.kjdautils.internal.di.DIService
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

class ConversationService(val dc: Discord, private val config: KConfiguration, val diService: DIService) {
    private var availableConversations = mutableListOf<Conversation>()
    private val activeConversations = mutableListOf<ConversationStateContainer>()

    fun hasConversation(userId: String) = activeConversations.any { it.userId == userId }
    private fun getConversationState(userId: String) = activeConversations.first { it.userId == userId }
    private fun getCurrentStep(conversationState: ConversationStateContainer) = conversationState.conversation.steps[conversationState.currentStep]

    fun createConversation(userId: String, guildId: String, conversationName: String) {
        if (hasConversation(userId)) return

        val user = dc.getUserById(userId)

        if (user != null && !user.isBot) {
            val conversation = availableConversations.first { it.name == conversationName }
            activeConversations.add(ConversationStateContainer(userId, guildId, mutableListOf(), conversation, 0, dc))
            sendToUser(userId, getCurrentStep(getConversationState(userId)).prompt)
        }
    }

    fun handleResponse(userId: String, event: PrivateMessageReceivedEvent) {
        val conversationState = getConversationState(userId)
        val currentStep = getCurrentStep(conversationState)
        val totalSteps = conversationState.conversation.steps.size
        val response = parseResponse(event.message, getCurrentStep(conversationState))

        if (response is ArgumentResult.Error) {
            sendToUser(userId, response.error)
            sendToUser(userId, currentStep.prompt)
        } else {
            conversationState.responses.add(response)
            if (conversationState.currentStep < (totalSteps - 1)) {
                conversationState.currentStep++
                sendToUser(conversationState.userId, getCurrentStep(conversationState).prompt)
            } else {
                conversationState.conversation.onComplete.invoke(conversationState)
                activeConversations.remove(conversationState)
            }
        }
    }

    fun registerConversations(path: String) {
        Reflections(path, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Convo::class.java).forEach {
            availableConversations.add(diService.invokeReturningMethod(it) as Conversation)
        }
    }

    private fun parseResponse(message: Message, step: Step): Any {
        val commandStruct = CommandStruct("", message.contentStripped.split(" "), false)
        val commandEvent = CommandEvent(commandStruct, message, commandStruct.commandArgs, CommandsContainer(), false, dc)
        val result = step.expect.convert(message.contentStripped, commandEvent.commandStruct.commandArgs, commandEvent)

        return when (result) {
            is ArgumentResult.Single -> result.result
            is ArgumentResult.Multiple -> result.result
            is ArgumentResult.Error -> result
        }
    }

    private fun sendToUser(userId: String, message: Any) {
        dc.getUserById(userId)?.let {
            if (message is MessageEmbed) it.sendPrivateMessage(message) else it.sendPrivateMessage(message as String)
        }
    }
}
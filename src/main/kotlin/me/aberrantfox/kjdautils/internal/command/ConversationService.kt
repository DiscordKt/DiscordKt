package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.Conversation
import me.aberrantfox.kjdautils.api.dsl.ConversationStateContainer
import me.aberrantfox.kjdautils.api.dsl.Convo
import me.aberrantfox.kjdautils.api.dsl.KJDAConfiguration
import me.aberrantfox.kjdautils.api.dsl.Step
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent
import org.reflections.Reflections
import org.reflections.scanners.FieldAnnotationsScanner
import org.reflections.scanners.MethodAnnotationsScanner

class ConversationService(val jda: JDA, private val config: KJDAConfiguration) {
    private var availableConversations = mutableListOf<Conversation>()
    private val activeConversations = mutableListOf<ConversationStateContainer>()

    fun hasConversation(userId: String) = activeConversations.any { it.userId == userId }
    private fun getConversationState(userId: String) = activeConversations.first { it.userId == userId }
    private fun getCurrentStep(conversationState: ConversationStateContainer) = conversationState.conversation.steps[conversationState.currentStep]

    fun createConversation(userId: String, guildId: String, conversationName: String) {
        if (hasConversation(userId) || jda.getUserById(userId).isBot) return

        val conversation = availableConversations.first { it.name == conversationName }
        activeConversations.add(ConversationStateContainer(userId, guildId, mutableListOf(), conversation, 0, jda))
        sendToUser(userId, getCurrentStep(getConversationState(userId)).prompt)
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
            availableConversations.add(it.invoke(null) as Conversation)
        }
    }

    private fun parseResponse(message: Message, step: Step): Any {
        val commandStruct = cleanCommandMessage(message.contentRaw, config)
        val commandEvent = CommandEvent(commandStruct, message, commandStruct.commandArgs, CommandsContainer(), false)
        val result = step.expect.convert(message.contentStripped, commandEvent.commandStruct.commandArgs, commandEvent)

        return when (result) {
            is ArgumentResult.Single -> result.result
            is ArgumentResult.Multiple -> result.result
            is ArgumentResult.Error -> result
        }
    }

    private fun sendToUser(userId: String, message: Any) {
        val user = jda.getUserById(userId)
        if (message is MessageEmbed) user.sendPrivateMessage(message, DefaultLogger()) else user.sendPrivateMessage(message as String, DefaultLogger())
    }
}

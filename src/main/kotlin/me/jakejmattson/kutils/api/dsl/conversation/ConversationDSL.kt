@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.conversation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.services.ConversationResult
import me.jakejmattson.kutils.internal.command.RawInputs
import me.jakejmattson.kutils.internal.utils.Responder
import net.dv8tion.jda.api.entities.*

private class ExitException : Exception("Conversation exited early.")
private class DmException : Exception("Message failed to deliver.")

data class ConversationStateContainer(val discord: Discord,
                                      val user: User,
                                      override val channel: MessageChannel,
                                      var exitString: String? = null) : Responder {

    private val inputChannel = Channel<Message>()
    val botMessageIds = mutableListOf<String>()
    val userMessageIds = mutableListOf<String>()

    val previousBotMessageId
        get() = botMessageIds.last()

    val previousUserMessageId
        get() = userMessageIds.last()

    internal suspend fun acceptMessage(message: Message) {
        inputChannel.send(message)
    }

    @Throws(DmException::class)
    fun <T> blockingPromptUntil(argumentType: ArgumentType<T>, initialPrompt: () -> Any, until: (T) -> Boolean, errorMessage: () -> Any): T {
        var value: T = blockingPrompt(argumentType, initialPrompt)

        while (!until.invoke(value)) {
            sendPrompt(errorMessage.invoke())
            value = blockingPrompt(argumentType, initialPrompt)
        }

        return value
    }

    @Throws(DmException::class)
    fun <T> blockingPrompt(argumentType: ArgumentType<T>, prompt: () -> Any): T {
        val promptValue = prompt.invoke()

        require(!argumentType.isOptional) { "Conversation arguments cannot be optional" }
        require(promptValue is String || promptValue is MessageEmbed) { "Prompt must be a String or a MessageEmbed" }

        return retrieveValidResponse(argumentType, promptValue)
    }

    private fun <T> retrieveValidResponse(argumentType: ArgumentType<*>, prompt: Any): T = runBlocking<T> {
        sendPrompt(prompt)
        retrieveResponse(argumentType) ?: retrieveValidResponse(argumentType, prompt)
    }

    private suspend fun <T> retrieveResponse(argumentType: ArgumentType<*>) = select<T?> {
        inputChannel.onReceive { input ->
            userMessageIds.add(input.id)

            if (input.contentStripped == exitString)
                throw ExitException()

            when (val result = parseResponse(argumentType, input)) {
                is ArgumentResult.Success -> result.result as T
                is ArgumentResult.Error -> {
                    respond(result.error)
                    null
                }
            }
        }
    }

    private fun parseResponse(argumentType: ArgumentType<*>, message: Message): ArgumentResult<*> {
        val rawInputs = RawInputs(message.contentRaw, "", message.contentStripped.split(" "), 0)
        val commandEvent = CommandEvent<Nothing>(rawInputs, CommandsContainer(), DiscordContext(discord, message))
        return argumentType.convert(message.contentStripped, commandEvent.rawInputs.commandArgs, commandEvent)
    }

    private fun sendPrompt(prompt: Any) {
        when (prompt) {
            is String -> channel.sendMessage(prompt).queue { botMessageIds.add(it.id) }
            is MessageEmbed -> channel.sendMessage(prompt).queue { botMessageIds.add(it.id) }
            else -> throw IllegalArgumentException("Prompt must be a String or a MessageEmbed")
        }
    }
}

class ConversationBuilder(private val exitString: String?, private val block: (ConversationStateContainer) -> Unit) {
    private lateinit var stateContainer: ConversationStateContainer

    @PublishedApi
    internal fun start(conversationStateContainer: ConversationStateContainer, onEnd: () -> Unit): ConversationResult {
        conversationStateContainer.exitString = exitString
        stateContainer = conversationStateContainer

        return try {
            block.invoke(conversationStateContainer)
            ConversationResult.COMPLETE
        } catch (e: ExitException) {
            ConversationResult.EXITED
        } catch (e: DmException) {
            ConversationResult.CANNOT_DM
        } finally {
            onEnd.invoke()
        }
    }

    @PublishedApi
    internal suspend fun acceptMessage(message: Message) = stateContainer.acceptMessage(message)
}

fun conversation(exitString: String? = null, block: ConversationStateContainer.() -> Unit) = ConversationBuilder(exitString, block)
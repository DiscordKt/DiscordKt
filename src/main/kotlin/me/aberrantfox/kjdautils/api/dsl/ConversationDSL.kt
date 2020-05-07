package me.aberrantfox.kjdautils.api.dsl

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.command.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.command.DiscordContext
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.CommandStruct
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User

private class ExitException() : Exception("Conversation exited early.")

data class ConversationStateContainer(val user: User,
                                      val discord: Discord,
                                      var exitString: String? = null) {

    private val inputChannel = Channel<Message>()

    internal suspend fun acceptMessage(message: Message) {
        inputChannel.send(message)
    }

    fun <T> blockingPromptUntil(argumentType: ArgumentType<T>, initialPrompt: () -> Any, until: (T) -> Boolean, errorMessage: () -> Any): T {
        var value: T = blockingPrompt(argumentType, initialPrompt)

        while (!until.invoke(value)) {
            sendPrompt(errorMessage.invoke())
            value = blockingPrompt(argumentType, initialPrompt)
        }

        return value
    }

    fun <T> blockingPrompt(argumentType: ArgumentType<T>, prompt: () -> Any): T {
        val promptValue = prompt.invoke()

        require(!argumentType.isOptional) { "Conversation arguments cannot be optional" }
        require(promptValue is String || promptValue is MessageEmbed) { "Prompt must be a String or a MessageEmbed" }

        return retrieveValidResponse(argumentType, promptValue)
    }

    @Throws(ExitException::class)
    private fun <T> retrieveValidResponse(argumentType: ArgumentType<*>, prompt: Any): T = runBlocking<T> {
        sendPrompt(prompt)
        retrieveResponse(argumentType) ?: retrieveValidResponse(argumentType, prompt)
    }

    @Throws(ExitException::class)
    private suspend fun <T> retrieveResponse(argumentType: ArgumentType<*>) = select<T?> {
        inputChannel.onReceive { input ->
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
        val commandStruct = CommandStruct("", message.contentStripped.split(" "), false)
        val discordContext = DiscordContext(false, discord, message)
        val commandEvent = CommandEvent<Nothing>(commandStruct, CommandsContainer(), discordContext)
        return argumentType.convert(message.contentStripped, commandEvent.commandStruct.commandArgs, commandEvent)
    }

    private fun sendPrompt(prompt: Any) {
        when (prompt) {
            is String -> respond(prompt)
            is MessageEmbed -> respond(prompt)
            else -> throw IllegalArgumentException("Prompt must be a String or a MessageEmbed")
        }
    }

    fun respond(message: String) = user.sendPrivateMessage(message)
    fun respond(embed: MessageEmbed) = user.sendPrivateMessage(embed)
}

class ConversationBuilder(private val exitString: String?, private val block: (ConversationStateContainer) -> Unit) {
    private lateinit var stateContainer: ConversationStateContainer

    @PublishedApi
    internal fun start(conversationStateContainer: ConversationStateContainer, onEnd: () -> Unit): Boolean {
        conversationStateContainer.exitString = exitString
        stateContainer = conversationStateContainer

        try {
            block.invoke(conversationStateContainer)
        }
        catch (e: ExitException) {
            onEnd.invoke()
            return false
        }

        onEnd.invoke()
        return true
    }

    @PublishedApi
    internal suspend fun acceptMessage(message: Message) = stateContainer.acceptMessage(message)
}

fun conversation(exitString: String? = null, block: ConversationStateContainer.() -> Unit) = ConversationBuilder(exitString, block)
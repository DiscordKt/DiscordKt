package me.aberrantfox.kjdautils.api.dsl

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.*

data class ConversationStateContainer(val user: User,
                                      val guild: Guild,
                                      val discord: Discord) {

    private val inputChannel = Channel<Message>()

    internal suspend fun acceptMessage(message: Message) {
        inputChannel.send(message)
    }

    fun <T> promptUntil(argumentType: ArgumentType<T>, initialPrompt: () -> Any, until: (T) -> Boolean, errorMessage: () -> Any): T {
        var value: T = prompt(argumentType, initialPrompt)

        while (!until.invoke(value)) {
            sendPrompt(errorMessage.invoke())
            value = prompt(argumentType, initialPrompt)
        }

        return value
    }

    fun <T> prompt(argumentType: ArgumentType<T>, prompt: () -> Any): T = runBlocking {

        val promptValue = prompt.invoke()

        require(!argumentType.isOptional) { "Conversation arguments cannot be optional" }
        require(promptValue is String || promptValue is MessageEmbed) { "Prompt must be a String or a MessageEmbed" }

        fun parseResponse(message: Message): ArgumentResult<*> {
            val commandStruct = CommandStruct("", message.contentStripped.split(" "), false)
            val discordContext = DiscordContext(false, discord, message)
            val commandEvent = CommandEvent<Nothing>(commandStruct, CommandsContainer(), discordContext)
            return argumentType.convert(message.contentStripped, commandEvent.commandStruct.commandArgs, commandEvent)
        }

        sendPrompt(promptValue)

        var finalResponse: T? = null

        while (finalResponse == null) {
            finalResponse = select {
                inputChannel.onReceive { input ->
                    val result = parseResponse(input)

                    if (result is ArgumentResult.Error) {
                        respond(result.error)
                        sendPrompt(promptValue)
                        null
                    } else {
                        result as ArgumentResult.Success
                        result.result as T
                    }
                }
            }
        }

        finalResponse!!
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

class Conversation(val name: String, private val block: (ConversationStateContainer) -> Unit) {
    private lateinit var stateContainer: ConversationStateContainer

    internal fun start(conversationStateContainer: ConversationStateContainer, onEnd: () -> Unit) {
        stateContainer = conversationStateContainer
        block.invoke(conversationStateContainer)
        onEnd.invoke()
    }

    internal suspend fun acceptMessage(message: Message) {
        stateContainer.acceptMessage(message)
    }
}

fun conversation(name: String, block: ConversationStateContainer.() -> Unit) = Conversation(name, block)

@Target(AnnotationTarget.FUNCTION)
annotation class Convo
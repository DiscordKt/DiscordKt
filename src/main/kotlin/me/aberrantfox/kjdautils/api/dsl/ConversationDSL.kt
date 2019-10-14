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

    internal val inputChannel = Channel<Message>()

    fun <T> promptFor(argumentType: ArgumentType<T>, prompt: () -> Any): T = runBlocking {
        fun parseResponse(message: Message): ArgumentResult<*> {
            val commandStruct = CommandStruct("", message.contentStripped.split(" "), false)
            val discordContext = DiscordContext(false, discord, message)
            val commandEvent = CommandEvent<Nothing>(commandStruct, CommandsContainer(), discordContext)
            return argumentType.convert(message.contentStripped, commandEvent.commandStruct.commandArgs, commandEvent)
        }

        sendPrompt(prompt.invoke())

        var finalResponse: T? = null

        while (finalResponse == null) {
            finalResponse = select {
                inputChannel.onReceive { input ->
                    println("Channel received message with content: ${input.contentRaw}")

                    val result = parseResponse(input)

                    if (result is ArgumentResult.Error) {
                        respond(result.error)
                        sendPrompt(prompt.invoke())
                        return@onReceive null
                    }

                    result as ArgumentResult.Success
                    result.result as T
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

    fun respond(msg: String) = user.sendPrivateMessage(msg)
    fun respond(embed: MessageEmbed) = user.sendPrivateMessage(embed)
}

class Conversation(val name: String, private val block: (ConversationStateContainer) -> Unit) {
    internal lateinit var stateContainer: ConversationStateContainer

    fun start(conversationStateContainer: ConversationStateContainer) {
        stateContainer = conversationStateContainer
        block.invoke(conversationStateContainer)
    }
}

fun conversation(name: String, block: ConversationStateContainer.() -> Unit) = Conversation(name, block)

@Target(AnnotationTarget.FUNCTION)
annotation class Convo
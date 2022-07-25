package me.jakejmattson.discordkt.conversations

import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.commands.SlashCommandEvent

public class SlashConversationBuilder<T : TypeContainer>(
    discord: Discord,
    user: User,
    private val event: SlashCommandEvent<T>,
    exitString: String? = null,
    timeout: Long,
) : ConversationBuilder(discord, user, event.channel, exitString, timeout) {
    /**
     * All tokens of messages sent by the bot in this conversation.
     */
//    public var botResponse: PublicMessageInteractionResponseBehavior? = null
//    public val botFollowupMessages: MutableList<PublicFollowupMessage> = mutableListOf()

    /**
     * Prompt the user with a String. Re-prompt until the response converts correctly. Then apply a custom predicate as an additional check.
     *
     * @param argument The [Argument] that the prompt expects in response.
     * @param prompt The string message sent to the user as a prompt for information.
     * @param error The error String to send when the input fails the custom check.
     * @param isValid A predicate to determine whether the input is accepted.
     */
    @Throws(DmException::class)
    public override suspend fun <T> promptUntil(argument: Argument<*, T>, prompt: String, error: String, isValid: (T) -> Boolean): T {
        var value: T = prompt(argument, prompt)

        while (!isValid.invoke(value)) {
            respond(error)
            value = prompt(argument, prompt)
        }

        return value
    }

    /**
     * Prompt the user with text and/or embed.
     *
     * @param argument The [Argument] that the prompt expects in response.
     * @param text A String sent as part of the prompt.
     * @param embed The embed sent as part of the prompt.
     */
    public override suspend fun <I, O> prompt(argument: Argument<I, O>, text: String, embed: (suspend EmbedBuilder.() -> Unit)?): O {
        require(!argument.isOptional()) { "Conversation arguments cannot be optional" }

        createMessage(text, embed)

        return retrieveValidTextResponse(argument)
    }

    override suspend fun <S> promptButton(prompt: suspend ButtonPromptBuilder<S>.() -> Unit): S {
        val builder = SlashButtonPromptBuilder<S, T>()
        prompt.invoke(builder)
        val responder = builder.create(event, responders.lastOrNull())

        responders.add(responder)

//        if (message.first != null) {
//            botResponse = message.first
//        } else {
//            botFollowupMessages.add(message.second!!)
//        }

        return retrieveValidInteractionResponse(builder.valueMap)
    }

    override suspend fun promptSelect(vararg options: String, embed: suspend EmbedBuilder.() -> Unit): String {
        val responder = getResponder()

        val newResponder = responder.respond {
            createSelectMessage(options, embed, this)
        }

        responders.add(newResponder)

//        if (botResponse == null) {
//            botResponse = event.interaction!!.respondPublic {
//                createSelectMessage(options, embed, this)
//            }
//        } else {
//            botFollowupMessages.add(botResponse!!.createPublicFollowup {
//                createSelectMessage(options, embed, this)
//            })
//        }

        return retrieveValidInteractionResponse(options.associateWith { it })
    }

    private fun getResponder() =
        responders.lastOrNull() ?: SlashResponder(event)

    override suspend fun respond(message: Any, embed: (suspend EmbedBuilder.() -> Unit)?): Message {
        createMessage(message.toString(), embed)

        return responders.last().ofMessage
    }

//    private suspend fun getLastBotResponseAsMessage(): Message =
//        if (botFollowupMessages.isEmpty()) {
//            val messageData = botResponse!!.kord.rest.interaction
//                .getInteractionResponse(botResponse!!.applicationId, botResponse!!.token)
//                .toData()
//
//            Message(messageData, event.discord.kord)
//        } else {
//            botFollowupMessages.last().message
//        }

    private suspend fun createMessage(text: String = "", embed: (suspend EmbedBuilder.() -> Unit)? = null) {
        val newResponder = getResponder().respond {
            content = text.takeIf { it.isNotBlank() }

            if (embed != null) {
                embed { embed(this) }
            }
        }

        responders.add(newResponder)
//        if (botResponse == null) {
//            val response = event.respondPublic(text, embed)
//
//            if (response !is PublicMessageInteractionResponseBehavior) throw RuntimeException("what")
//
//            botResponse = response
//
//            return
//        }
//
//        val followupMessage = botResponse!!.createPublicFollowup {
//            content = text
//
//            if (embed != null) {
//                embed { embed.invoke(this) }
//            }
//        }
//
//        botFollowupMessages.add(followupMessage)
    }

    override suspend fun interactionIsOnLastBotMessage(interaction: ComponentInteraction): Boolean =
        interaction.message.id == responders.last().ofMessage.id
}
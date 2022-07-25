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
    @Throws(DmException::class)
    public override suspend fun <T> promptUntil(argument: Argument<*, T>, prompt: String, error: String, isValid: (T) -> Boolean): T {
        var value: T = prompt(argument, prompt)

        while (!isValid.invoke(value)) {
            respond(error)
            value = prompt(argument, prompt)
        }

        return value
    }

    @Throws(DmException::class, TimeoutException::class)
    public override suspend fun <I, O> prompt(argument: Argument<I, O>, text: String, embed: (suspend EmbedBuilder.() -> Unit)?): O {
        require(!argument.isOptional()) { "Conversation arguments cannot be optional" }

        createMessage(text, embed)

        return retrieveValidTextResponse(argument)
    }

    @Throws(DmException::class, TimeoutException::class)
    override suspend fun <S> promptButton(prompt: suspend ButtonPromptBuilder<S>.() -> Unit): S {
        val builder = SlashButtonPromptBuilder<S, T>()
        prompt.invoke(builder)
        val responder = builder.create(event, responders.lastOrNull())

        responders.add(responder)

        return retrieveValidInteractionResponse(builder.valueMap)
    }

    @Throws(DmException::class, TimeoutException::class)
    override suspend fun promptSelect(vararg options: String, embed: suspend EmbedBuilder.() -> Unit): String {
        val responder = getResponder()

        val newResponder = responder.respond {
            createSelectMessage(options, embed, this)
        }

        responders.add(newResponder)

        return retrieveValidInteractionResponse(options.associateWith { it })
    }

    private fun getResponder() =
        responders.lastOrNull() ?: SlashResponder(event)

    override suspend fun respond(message: Any, embed: (suspend EmbedBuilder.() -> Unit)?): Message {
        createMessage(message.toString(), embed)

        return responders.last().ofMessage
    }

    private suspend fun createMessage(text: String = "", embed: (suspend EmbedBuilder.() -> Unit)? = null) {
        val newResponder = getResponder().respond {
            content = text.takeIf { it.isNotBlank() }

            if (embed != null) {
                embed { embed(this) }
            }
        }

        responders.add(newResponder)
    }

    override suspend fun interactionIsOnLastBotMessage(interaction: ComponentInteraction): Boolean =
        interaction.message.id == responders.last().ofMessage.id
}
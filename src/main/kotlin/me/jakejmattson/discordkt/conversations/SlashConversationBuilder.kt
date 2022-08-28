package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.commands.SlashCommandEvent
import me.jakejmattson.discordkt.conversations.responders.SlashResponder
import me.jakejmattson.discordkt.prompts.SimpleSelectBuilder

public class SlashConversationBuilder<T : TypeContainer>(
    discord: Discord,
    user: User,
    private val event: SlashCommandEvent<T>,
    exitString: String? = null,
    timeout: Long,
) : ConversationBuilder(discord, user, event.channel, exitString, timeout) {
    public override val userMessageIds: MutableList<Snowflake>
        get() = responders.mapNotNull { it.userResponse?.id }.toMutableList()

    public override val botMessageIds: MutableList<Snowflake>
        get() = responders.map { it.promptMessage.id }.toMutableList()

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

        return retrieveValidInteractionResponse(builder.valueMap).first()
    }

    @Throws(DmException::class, TimeoutException::class)
    override suspend fun promptSelect(builder: SimpleSelectBuilder.() -> Unit): List<String> {
        val responder = getResponder()

        val newResponder = responder.respond {
            createSelectMessage(builder, this)
        }

        responders.add(newResponder)

        return retrieveValidInteractionResponse(emptyMap())
    }

    private fun getResponder() =
        responders.lastOrNull() ?: SlashResponder(event)

    override suspend fun respond(message: Any, embed: (suspend EmbedBuilder.() -> Unit)?): Message {
        createMessage(message.toString(), embed)

        return responders.last().promptMessage
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
        interaction.message.id == responders.last().promptMessage.id
}
package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.prompts.SimpleSelectBuilder

/** @suppress DSL backing
 *
 * @param discord The discord instance.
 * @param user The user that the conversation is happening with.
 * @param channel The channel that the conversation is happening in.
 * @param exitString A String entered by the user to exit the conversation.
 */
public class PlainConversationBuilder(
    discord: Discord,
    user: User,
    channel: MessageChannel,
    exitString: String? = null,
    timeout: Long,
) : ConversationBuilder(discord, user, channel, exitString, timeout) {
    public override val userMessageIds: MutableList<Snowflake> = mutableListOf()
    public override val botMessageIds: MutableList<Snowflake> = mutableListOf()

    @Throws(DmException::class)
    public override suspend fun <T> promptUntil(argument: Argument<*, T>, prompt: String, error: String, isValid: (T) -> Boolean): T {
        var value: T = prompt(argument, prompt)

        while (!isValid.invoke(value)) {
            channel.createMessage(error).also { it.let { botMessageIds.add(it.id) } }
            value = prompt(argument, prompt)
        }

        return value
    }

    @Throws(DmException::class, TimeoutException::class)
    public override suspend fun <I, O> prompt(argument: Argument<I, O>, text: String, embed: (suspend EmbedBuilder.() -> Unit)?): O {
        require(!argument.isOptional()) { "Conversation arguments cannot be optional" }

        val message = channel.createMessage {
            content = text.takeIf { it.isNotBlank() }

            if (embed != null) {
                val builder = EmbedBuilder()
                embed.invoke(builder)
                embeds.add(builder)
            }
        }

        botMessageIds.add(message.id)

        return retrieveValidTextResponse(argument)
    }

    @Throws(DmException::class, TimeoutException::class)
    public override suspend fun <T> promptButton(prompt: suspend ButtonPromptBuilder<T>.() -> Unit): T {
        val builder = ButtonPromptBuilder<T>()
        prompt.invoke(builder)
        val responder = builder.create(channel, channel.lastMessage!!.asMessage())

        botMessageIds.add(responder.ofMessage.id)

        return retrieveValidInteractionResponse(builder.valueMap).first()
    }

    @Throws(DmException::class, TimeoutException::class)
    public override suspend fun promptSelect(builder: SimpleSelectBuilder.() -> Unit): List<String> {
        val message = channel.createMessage {
            createSelectMessage(builder, this)
        }

        botMessageIds.add(message.id)

        return retrieveValidInteractionResponse(emptyMap())
    }

    override suspend fun interactionIsOnLastBotMessage(interaction: ComponentInteraction): Boolean =
        interaction.message.id == previousBotMessageId
}
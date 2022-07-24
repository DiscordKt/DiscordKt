package me.jakejmattson.discordkt.conversations

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.dsl.uuid

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
    /**
     * All ID's of messages sent by the bot in this conversation.
     */
    public val botMessageIds: MutableList<Snowflake> = mutableListOf()
    /**
     * The ID of the most recent message sent by the bot in this conversation.
     */
    public val previousBotMessageId: Snowflake
        get() = botMessageIds.last()

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
            channel.createMessage(error).also { it.let { botMessageIds.add(it.id) } }
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

    /**
     * Prompt the user with an embed and the provided buttons.
     * Requires a call to both [ButtonPromptBuilder.embed] and [ButtonPromptBuilder.buttons].
     *
     * @param prompt The [builder][ButtonPromptBuilder]
     */
    @Throws(DmException::class, TimeoutException::class)
    public override suspend fun <T> promptButton(prompt: suspend ButtonPromptBuilder<T>.() -> Unit): T {
        val builder = ButtonPromptBuilder<T>()
        prompt.invoke(builder)
        val message = builder.create(channel)

        botMessageIds.add(message.id)

        return retrieveValidInteractionResponse(builder.valueMap)
    }

    /**
     * Prompt the user with a select menu.
     *
     * @param options The options that can be selected by the user
     * @param embed The embed sent as part of the prompt.
     */
    @Throws(DmException::class, TimeoutException::class)
    public override suspend fun promptSelect(vararg options: String, embed: suspend EmbedBuilder.() -> Unit): String {
        val message = channel.createMessage {
            createSelectMessage(options, embed, this)
        }

        botMessageIds.add(message.id)

        return retrieveValidInteractionResponse(options.associateWith { it })
    }

    override suspend fun interactionIsOnLastBotMessage(interaction: ComponentInteraction): Boolean =
        interaction.message.id == previousBotMessageId
}
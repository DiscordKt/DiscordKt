package me.jakejmattson.discordkt.commands

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.interaction.response.InteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.addReaction
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.dsl.Responder

/**
 * Data class containing the raw information from the command execution.
 *
 * @property rawMessageContent The message as it was sent from the user - no modifications.
 * @property commandName The command name parses from the raw content. This is not necessarily a valid command.
 * @property commandArgs The arguments provided to the command execution.
 * @property prefixCount The number of prefixes used to invoke this command.
 */
public data class RawInputs(
    val rawMessageContent: String,
    val commandName: String,
    val prefixCount: Int,
    val commandArgs: List<String> = rawMessageContent.split(" ").drop(1)
)

/**
 * The discord context of the command execution.
 *
 * @property discord The [Discord] instance.
 * @property message The Message that invoked this command.
 * @property guild The Guild this command was invoked in.
 * @property author The User who invoked this command.
 * @property channel The MessageChannel this command was invoked in.
 * @property prefix The prefix used to invoke this command.
 */
public open class DiscordContext(public val discord: Discord,
                                 public open val message: Message?,
                                 public val author: User,
                                 override val channel: MessageChannelBehavior,
                                 public open val guild: Guild?) : Responder {
    /**
     * Determine the relevant prefix from the configured prefix block.
     */
    public suspend fun prefix(): String = discord.configuration.prefix.invoke(this)
}

/**
 * A generic command execution event.
 *
 * @property rawInputs The [RawInputs] of the command.
 * @property discord The [Discord] instance.
 * @property message The Message that invoked this command.
 * @property author The User who invoked this command.
 * @property channel The MessageChannel this command was invoked in.
 * @property guild The (nullable) guild this command was invoked in.
 * @property args The parsed input to the command.
 * @property command The [Command] that is resolved from the invocation.
 * @property context The [DiscordContext] of this event.
 */
public open class CommandEvent<T : TypeContainer>(
    public open val rawInputs: RawInputs,
    public open val discord: Discord,
    public open val message: Message?,
    public open val author: User,
    override val channel: MessageChannel,
    public open val guild: Guild?) : Responder {

    public lateinit var args: T

    public val command: Command?
        get() = discord.commands[rawInputs.commandName]

    public val context: DiscordContext
        get() = DiscordContext(discord, message, author, channel, guild)

    /**
     * Try to resolve the member from the user/guild data.
     */
    public suspend fun getMember(): Member? = guild?.getMember(author.id)

    /**
     * Determine the relevant prefix in the current context.
     */
    public suspend fun prefix(): String = message?.let { discord.configuration.prefix.invoke(DiscordContext(discord, it, author, channel, guild)) }
        ?: "/"

    /**
     * Add a reaction to the command invocation message.
     */
    public suspend fun reactWith(emoji: DiscordEmoji): Unit? = message?.addReaction(emoji)

    /**
     * Clone this event's context data with new inputs.
     */
    public open fun clone(input: RawInputs): CommandEvent<T> = CommandEvent(input, discord, message, author, channel, guild)

    internal fun isFromGuild() = guild != null
}

/**
 * An event that can only be fired in a guild.
 */
public data class GuildCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    override val message: Message,
    override val author: User,
    override val channel: GuildMessageChannel,
    override val guild: Guild) : CommandEvent<T>(rawInputs, discord, message, author, channel, guild)

/**
 * An event that can only be fired in a DM.
 */
public data class DmCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    override val message: Message,
    override val author: User,
    override val channel: DmChannel,
    @Deprecated("There is no guild within a DmCommandEvent.", level = DeprecationLevel.ERROR)
    override val guild: Guild? = null) : CommandEvent<T>(rawInputs, discord, message, author, channel, null)

/**
 * An event fired by a slash command.
 * @param interaction Initial [ApplicationCommandInteraction] event.
 */
public open class SlashCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    @Deprecated("A slash command cannot access its message.", level = DeprecationLevel.ERROR)
    override val message: Message?,
    override val author: User,
    override val channel: MessageChannel,
    override val guild: Guild? = null,
    public open val interaction: ApplicationCommandInteraction?) : CommandEvent<T>(rawInputs, discord, message, author, channel, null) {

    /**
     * Custom ephemeral respond function.
     *
     * @param message The content of the message to respond.
     * @param ephemeral Whether this message should be ephemeral.
     */
    public suspend fun respond(message: Any, ephemeral: Boolean = true): InteractionResponseBehavior? =
        if (interaction != null) {
            val defer = if (ephemeral) interaction!!.deferEphemeralResponse() else interaction!!.deferPublicResponse()
            defer.respond { content = message.toString() }
        }
        else {
            super.respond(message)
            null
        }

    /**
     * Custom ephemeral respond function.
     *
     * @param ephemeral Whether this message should be ephemeral.
     * @param embedBuilder
     */
    public suspend fun respond(ephemeral: Boolean = true, embedBuilder: suspend EmbedBuilder.() -> Unit): InteractionResponseBehavior? =
        if (interaction != null) {
            val defer = if (ephemeral) interaction!!.deferEphemeralResponse() else interaction!!.deferPublicResponse()
            defer.respond { embed { embedBuilder.invoke(this) } }
        }
        else {
            super.respond(embedBuilder)
            null
        }

    override suspend fun respond(message: Any): List<Message> =
        interaction?.deferEphemeralResponse()?.let {
            it.respond { content = message.toString() }
            emptyList()
        } ?: super.respond(message)

    override suspend fun respond(embedBuilder: suspend EmbedBuilder.() -> Unit): Message? =
        if (interaction == null) {
            super.respond(embedBuilder)
        } else {
            interaction!!.deferEphemeralResponse().respond { embed { embedBuilder.invoke(this) } }
            null
        }
}

/**
 * An event fired by a guild slash command.
 */
public data class GuildSlashCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    @Deprecated("A slash command cannot access its message.", level = DeprecationLevel.ERROR)
    override val message: Message?,
    override val author: User,
    override val channel: MessageChannel,
    override val guild: Guild,
    override val interaction: ApplicationCommandInteraction?
) : SlashCommandEvent<T>(rawInputs, discord, message, author, channel, guild, interaction)
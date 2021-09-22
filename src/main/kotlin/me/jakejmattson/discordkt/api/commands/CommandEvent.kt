package me.jakejmattson.discordkt.api.commands

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.addReaction
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.TypeContainer
import me.jakejmattson.discordkt.api.dsl.Responder

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
                                 public open val message: Message,
                                 public open val guild: Guild?,
                                 public val author: User = message.author!!,
                                 override val channel: MessageChannelBehavior = message.channel) : Responder {
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

    /**
     * Try to resolve the member from the user/guild data.
     */
    public suspend fun getMember(): Member? = guild?.getMember(author.id)

    /**
     * Determine the relevant prefix in the current context.
     */
    public suspend fun prefix(): String = message?.let { discord.configuration.prefix.invoke(DiscordContext(discord, it, guild, author, channel)) }
        ?: "/"

    /**
     * Add a reaction to the command invocation message.
     */
    public suspend fun reactWith(emoji: DiscordEmoji): Unit? = message?.addReaction(emoji)

    /**
     * Clone this event's context data with new inputs.
     */
    public open fun clone(input: RawInputs): CommandEvent<T> = CommandEvent<T>(input, discord, message, author, channel, guild)

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
 * @param ephemeralAck [EphemeralInteractionResponseBehavior] used for follow up.
 */
public open class SlashCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    @Deprecated("A slash command cannot access its message.", level = DeprecationLevel.ERROR)
    override val message: Message?,
    override val author: User,
    override val channel: MessageChannel,
    override val guild: Guild? = null,
    public open val ephemeralAck: EphemeralInteractionResponseBehavior?) : CommandEvent<T>(rawInputs, discord, message, author, channel, null) {

    override suspend fun respond(message: Any): List<Message> =
        if (ephemeralAck != null) {
            ephemeralAck!!.followUpEphemeral {
                content = message.toString()
            }

            emptyList()
        } else super.respond(message)

    override suspend fun respond(construct: suspend EmbedBuilder.() -> Unit): Message? =
        if (ephemeralAck != null) {
            ephemeralAck?.followUpEphemeral {
                embed {
                    construct.invoke(this)
                }
            }
            null
        } else super.respond(construct)
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
    override val ephemeralAck: EphemeralInteractionResponseBehavior?
) : SlashCommandEvent<T>(rawInputs, discord, message, author, channel, guild, ephemeralAck)
package me.jakejmattson.discordkt.api.dsl

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.addReaction
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.TypeContainer

/**
 * Data class containing the raw information from the command execution.
 *
 * @property rawMessageContent The message as it was sent from the user - no modifications.
 * @property commandName The command name parses from the raw content. This is not necessarily a valid command.
 * @property commandArgs The arguments provided to the command execution.
 * @property prefixCount The number of prefixes used to invoke this command.
 */
data class RawInputs(
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
open class DiscordContext(val discord: Discord,
                          open val message: Message,
                          open val guild: Guild?,
                          val author: User = message.author!!,
                          override val channel: MessageChannelBehavior = message.channel) : Responder {
    /**
     * Determine the relevant prefix from the configured prefix block.
     */
    suspend fun prefix() = discord.configuration.prefix.invoke(this)
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
open class CommandEvent<T : TypeContainer>(
    open val rawInputs: RawInputs,
    open val discord: Discord,
    open val message: Message,
    open val author: User,
    override val channel: MessageChannel,
    open val guild: Guild?) : Responder {

    lateinit var args: T

    val command
        get() = discord.commands[rawInputs.commandName]

    /**
     * Try to resolve the member from the user/guild data.
     */
    suspend fun getMember() = guild?.getMember(author.id)

    /**
     * Determine the relevant prefix in the current context.
     */
    suspend fun prefix() = discord.configuration.prefix.invoke(DiscordContext(discord, message, guild, author, channel))

    /**
     * Add a reaction to the command invocation message.
     */
    suspend fun reactWith(emoji: DiscordEmoji) = message.addReaction(emoji)

    /**
     * Clone this event's context data with new inputs.
     */
    open fun clone(input: RawInputs) = CommandEvent<T>(input, discord, message, author, channel, guild)

    internal fun isFromGuild() = guild != null
}

/**
 * An event that can only be fired in a guild.
 */
data class GuildCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    override val message: Message,
    override val author: User,
    override val channel: TextChannel,
    override val guild: Guild) : CommandEvent<T>(rawInputs, discord, message, author, channel, guild)

/**
 * An event that can only be fired in a DM.
 */
data class DmCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    override val message: Message,
    override val author: User,
    override val channel: DmChannel,
    @Deprecated("There is no guild within a DmCommandEvent.", level = DeprecationLevel.ERROR)
    override val guild: Guild? = null) : CommandEvent<T>(rawInputs, discord, message, author, channel, null)

/**
 * An event fired by a slash command.
 */
data class SlashCommandEvent<T : TypeContainer>(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    @Deprecated("A slash command cannot access its message.", level = DeprecationLevel.ERROR)
    override val message: Message,
    override val author: User,
    override val channel: MessageChannel,
    override val guild: Guild? = null) : CommandEvent<T>(rawInputs, discord, message, author, channel, null)
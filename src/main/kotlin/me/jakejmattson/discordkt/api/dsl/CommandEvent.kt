package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.*
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.utils.Responder

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
    val commandArgs: List<String> = listOf(),
    val prefixCount: Int
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
open class DiscordContext(override val discord: Discord,
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
 * A command execution event containing the [RawInputs] and the relevant [DiscordContext].
 *
 * @property rawInputs The [RawInputs] of the command.
 * @property discord The [Discord] instance.
 * @property author The User who invoked this command.
 * @property message The Message that invoked this command.
 * @property channel The MessageChannel this command was invoked in.
 * @property command The [Command] that is resolved from the invocation.
 */
open class CommandEvent(
    open val rawInputs: RawInputs,
    override val discord: Discord,
    open val message: Message,
    open val author: User,
    override val channel: MessageChannel,
    open val guild: Guild?) : Responder {

    val command
        get() = discord.commands[rawInputs.commandName]

    suspend fun prefix() = discord.configuration.prefix.invoke(DiscordContext(discord, message, guild, author, channel))

    open fun clone(input: RawInputs) = CommandEvent(input, discord, message, author, channel, guild)
    internal fun isFromGuild() = guild != null
}

data class GuildCommandEvent(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    override val message: Message,
    override val author: User,
    override val channel: TextChannel,
    override val guild: Guild) : CommandEvent(rawInputs, discord, message, author, channel, guild)

data class DmCommandEvent(
    override val rawInputs: RawInputs,
    override val discord: Discord,
    override val message: Message,
    override val author: User,
    override val channel: DmChannel,
    @Deprecated("This field is always null in a DM. It only exists to fulfill the CommandEvent contract.", level = DeprecationLevel.ERROR)
    override val guild: Guild? = null) : CommandEvent(rawInputs, discord, message, author, channel, null)
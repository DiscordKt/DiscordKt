package me.jakejmattson.discordkt.commands

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.core.entity.interaction.GuildApplicationCommandInteraction
import me.jakejmattson.discordkt.Args1
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.dsl.SlashResponder

/**
 * The discord context of the command execution.
 *
 * @property discord The [Discord] instance.
 * @property message The Message that invoked this command.
 * @property guild The Guild this command was invoked in.
 * @property author The User who invoked this command.
 * @property channel The MessageChannel this command was invoked in.
 */
public open class DiscordContext(public val discord: Discord,
                                 public open val message: Message?,
                                 public val author: User,
                                 public val channel: MessageChannelBehavior,
                                 public open val guild: Guild?)

/**
 * A generic command execution event.
 *
 * @property discord The [Discord] instance.
 * @property author The User who invoked this command.
 * @property channel The MessageChannel this command was invoked in.
 * @property guild The (nullable) guild this command was invoked in.
 * @property args The parsed input to the command.
 * @property command The [Command] that is resolved from the invocation.
 * @property context The [DiscordContext] of this event.
 */
public interface CommandEvent<T : TypeContainer> : SlashResponder {
    public val command: Command
    public val discord: Discord
    public val author: User
    public val channel: MessageChannel
    public val guild: Guild?
    public val args: T

    public val context: DiscordContext
        get() = DiscordContext(discord, null, author, channel, guild)

    /**
     * Try to resolve the member from the user/guild data.
     */
    public suspend fun getMember(): Member? = guild?.getMember(author.id)
}

/**
 * An event fired by a slash command.
 * @param interaction Initial [ApplicationCommandInteraction] event.
 */
public open class SlashCommandEvent<T : TypeContainer>(
    override val command: Command,
    override val discord: Discord,
    override val author: User,
    override val channel: MessageChannel,
    override val guild: Guild? = null,
    public override val interaction: ApplicationCommandInteraction,
    override val args: T) : CommandEvent<T>

/**
 * An event fired by a guild slash command.
 */
public data class GuildSlashCommandEvent<T : TypeContainer>(
    override val command: Command,
    override val discord: Discord,
    override val author: User,
    override val channel: MessageChannel,
    override val guild: Guild,
    override val interaction: GuildApplicationCommandInteraction,
    override val args: T
) : SlashCommandEvent<T>(command, discord, author, channel, guild, interaction, args) {
    internal fun <A> toContextual(arg: A) = ContextEvent(command, discord, author, channel, guild, interaction, arg)
}

/**
 * An event fired by a contextual slash command.
 *
 * @property arg The single argument passed to this context command.
 */
public data class ContextEvent<T>(
    override val command: Command,
    override val discord: Discord,
    override val author: User,
    override val channel: MessageChannel,
    override val guild: Guild,
    override val interaction: GuildApplicationCommandInteraction,
    val arg: T
) : SlashCommandEvent<Args1<T>>(command, discord, author, channel, guild, interaction, Args1(arg))
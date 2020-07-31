package me.jakejmattson.discordkt.api.dsl.command

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.utils.Responder
import net.dv8tion.jda.api.entities.*

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
 * @property author The User who invoked this command.
 * @property guild The Guild this command was invoked in.
 * @property channel The MessageChannel this command was invoked in.
 * @property relevantPrefix The prefix used to invoke this command.
 */
data class DiscordContext(val discord: Discord,
                          val message: Message,
                          val author: User = message.author,
                          val guild: Guild? = if (message.isFromGuild) message.guild else null,
                          override val channel: MessageChannel = message.channel) : Responder {
    val relevantPrefix: String = discord.configuration.prefix.invoke(this)
}

/**
 * A command execution event containing the [RawInputs], [CommandsContainer], and the relevant [DiscordContext].
 *
 * @param rawInputs The [RawInputs] of the command.
 * @param container The [CommandsContainer] containing commands within DiscordKt.
 *
 * @property discord The [Discord] instance.
 * @property author The User who invoked this command.
 * @property message The Message that invoked this command.
 * @property channel The MessageChannel this command was invoked in.
 * @property guild The Guild this command was invoked in.
 * @property command The [Command] that is resolved from the invocation.
 * @property relevantPrefix The prefix used to invoke this command.
 * @property args The [GenericContainer] containing the converted input.
 */
data class CommandEvent<T : GenericContainer>(val rawInputs: RawInputs,
                                              val container: CommandsContainer,
                                              private val discordContext: DiscordContext) : Responder {
    val discord = discordContext.discord
    val author = discordContext.author
    val message = discordContext.message
    override val channel = discordContext.channel
    val guild = discordContext.guild
    val command = container[rawInputs.commandName]
    val relevantPrefix = discordContext.relevantPrefix

    lateinit var args: T

    /**
     * Clone this event with optional modifications.
     */
    fun cloneToGeneric(input: RawInputs = rawInputs,
                       commandsContainer: CommandsContainer = container,
                       context: DiscordContext = discordContext) =
        CommandEvent<GenericContainer>(input, commandsContainer, context)
}
package me.jakejmattson.kutils.api.dsl.command

import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.internal.utils.Responder
import net.dv8tion.jda.api.entities.*

/**
 * Data class containing the raw information from the command execution.
 */
data class RawInputs(
    val rawMessageContent: String,
    val commandName: String,
    val commandArgs: List<String> = listOf(),
    val prefixCount: Int
)

/**
 * The discord context of the command execution.
 */
data class DiscordContext(val discord: Discord,
                          val message: Message,
                          val author: User = message.author,
                          val guild: Guild? = if (message.isFromGuild) message.guild else null,
                          override val channel: MessageChannel = message.channel) : Responder {
    val relevantPrefix: String = discord.configuration.prefix.invoke(this)
}

/**
 * A command execution event.
 *
 * @see RawInputs
 * @see CommandsContainer
 * @see Discord
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
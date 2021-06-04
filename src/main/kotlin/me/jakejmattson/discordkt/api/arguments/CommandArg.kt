package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.Command
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.get

/**
 * Accepts a DiscordKt command by name.
 */
open class CommandArg(override val name: String = "Command") : ArgumentType<Command> {
    /**
     * Accepts a DiscordKt command by name.
     */
    companion object : CommandArg()

    override val description = "A DiscordKt command"

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Command> {
        val command = event.discord.commands[arg] ?: return Error(event.discord.locale.notFound)
        return Success(command)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = event.discord.commands.mapNotNull { it.names.firstOrNull() }
    override fun formatData(data: Command) = data.names.first()
}
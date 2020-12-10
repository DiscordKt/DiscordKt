package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*

/**
 * Accepts a DiscordKt command by name.
 */
open class CommandArg(override val name: String = "Command") : ArgumentType<Command> {
    /**
     * Accepts a DiscordKt command by name.
     */
    companion object : CommandArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Command> {
        val command = event.discord.commands[arg] ?: return Error("Not found")
        return Success(command)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = event.discord.commands.mapNotNull { it.names.firstOrNull() }
    override fun formatData(data: Command) = data.names.first()
}
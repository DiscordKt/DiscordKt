package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.*

/**
 * Accepts a DiscordKt command by name.
 */
open class CommandArg(override val name: String = "Command") : ArgumentType<Command>() {
    /**
     * Accepts a DiscordKt command by name.
     */
    companion object : CommandArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Command> {
        val command = event.container[arg.toLowerCase()]
            ?: return Error("Not found")

        return Success(command)
    }

    override fun generateExamples(event: CommandEvent<*>) = event.container.commands.mapNotNull { it.names.firstOrNull() }
    override fun formatData(data: Command) = data.names.first()
}
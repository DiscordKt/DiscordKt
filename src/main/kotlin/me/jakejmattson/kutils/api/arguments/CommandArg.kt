package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*

/**
 * Accepts a KUtils command.
 */
open class CommandArg(override val name: String = "Command") : ArgumentType<Command>() {
    companion object : CommandArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Command> {
        val command = event.container[arg.toLowerCase()]
            ?: return Error("Couldn't parse $name from $arg.")

        return Success(command)
    }

    override fun generateExamples(event: CommandEvent<*>) = event.container.commands.mapNotNull { it.names.firstOrNull() }
}
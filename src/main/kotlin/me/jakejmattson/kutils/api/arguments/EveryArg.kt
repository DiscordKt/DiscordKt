package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class EveryArg(override val name: String = "Text") : ArgumentType<String>() {
    companion object : EveryArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        if (args.size in 0..1 && arg.isEmpty())
            return ArgumentResult.Error("$name cannot be empty.")

        return ArgumentResult.Success(args.joinToString(" "), args.size)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("This is a sample sentence.")
}
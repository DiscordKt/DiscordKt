package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * Accepts a single character.
 */
open class CharArg(override val name: String = "Character") : ArgumentType<Char>() {
    companion object : CharArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Char> {
        return if (arg.length == 1)
            Success(arg[0])
        else
            Error("$name should be a single character.")
    }

    override fun generateExamples(event: CommandEvent<*>) = ('a'..'z').map { it.toString() }
}

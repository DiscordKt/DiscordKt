package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class CharArg(override val name: String = "Character") : ArgumentType<Char>() {
    companion object : CharArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Char> {
        return if (arg.length == 1)
            ArgumentResult.Success(arg[0])
        else
            ArgumentResult.Error("Invalid character argument.")
    }

    override fun generateExamples(event: CommandEvent<*>) = ('a'..'z').map { it.toString() }
}

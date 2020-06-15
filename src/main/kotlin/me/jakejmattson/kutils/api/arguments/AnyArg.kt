package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class AnyArg(override val name: String = "Any") : ArgumentType<String>() {
    companion object : AnyArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> =
        if (arg.isNotEmpty()) ArgumentResult.Success(arg) else ArgumentResult.Error("$name cannot be empty.")

    override fun generateExamples(event: CommandEvent<*>) = listOf(name)
}
package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * Accepts any (single) argument. Does not accept empty strings.
 */
open class AnyArg(override val name: String = "Any") : ArgumentType<String>() {
    /**
     * Accepts any (single) argument. Does not accept empty strings.
     */
    companion object : AnyArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> =
        if (arg.isNotEmpty()) Success(arg) else Error("$name cannot be empty.")

    override fun generateExamples(event: CommandEvent<*>) = listOf(name)
}
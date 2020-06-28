package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.stdlib.containsURl

/**
 * Accepts a URL.
 */
open class UrlArg(override val name: String = "URL") : ArgumentType<String>() {
    companion object : UrlArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        return if (arg.containsURl())
            ArgumentResult.Success(arg)
        else
            ArgumentResult.Error("Couldn't parse $name from $arg.")
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("http://www.google.com")
}
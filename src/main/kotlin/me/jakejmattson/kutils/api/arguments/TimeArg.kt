package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.ArgumentType
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.internal.utils.convertTimeString

/**
 * Accepts a group of time elements and returns the number of seconds as a double.
 */
open class TimeArg(override val name: String = "Time") : ArgumentType<Double>() {
    /**
     * Accepts a group of time elements and returns the number of seconds as a double.
     */
    companion object : TimeArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = convertTimeString(args)

    override fun generateExamples(event: CommandEvent<*>) = listOf("5 seconds", "5s")
}
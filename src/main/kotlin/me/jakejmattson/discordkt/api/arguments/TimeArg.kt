package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.internal.arguments.convertTimeString

/**
 * Accepts a group of time elements and returns the number of seconds as a double.
 */
open class TimeArg(override val name: String = "Time") : ArgumentType<Double>() {
    /**
     * Accepts a group of time elements and returns the number of seconds as a double.
     */
    companion object : TimeArg()

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>) = convertTimeString(args)

    override fun generateExamples(event: GlobalCommandEvent<*>) = listOf("5 seconds", "5s")

    override fun formatData(data: Double) = "$data seconds"
}
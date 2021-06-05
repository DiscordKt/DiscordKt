package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.internal.utils.convertTimeString

/**
 * Accepts a group of time elements and returns the number of seconds as a double.
 */
open class TimeArg(override val name: String = "Time") : ArgumentType<Double> {
    /**
     * Accepts a group of time elements and returns the number of seconds as a double.
     */
    companion object : TimeArg()

    override val description = internalLocale.timeArgDescription

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = convertTimeString(args)

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf("5 seconds", "5s")
    override fun formatData(data: Double) = "$data seconds"
}
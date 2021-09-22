package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.internal.utils.convertTimeString

/**
 * Accepts a group of time elements and returns the number of seconds as a double.
 */
public open class TimeArg(override val name: String = "Time",
                          override val description: String = internalLocale.timeArgDescription) : Argument<Double> {
    /**
     * Accepts a group of time elements and returns the number of seconds as a double.
     */
    public companion object : TimeArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Double> = convertTimeString(args)
    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("5 seconds", "5s")
    override fun formatData(data: Double): String = "$data seconds"
}
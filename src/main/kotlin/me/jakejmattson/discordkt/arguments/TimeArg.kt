package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a group of time elements and returns the number of seconds as a double.
 */
public open class TimeArg(override val name: String = "Time",
                          override val description: String = internalLocale.timeArgDescription) : StringArgument<Double> {
    /**
     * Accepts a group of time elements and returns the number of seconds as a double.
     */
    public companion object : TimeArg()

    override suspend fun transform(input: String, context: DiscordContext): Result<Double> {
        if (!input.matches(fullRegex)) {
            return Error(internalLocale.invalidFormat)
        }

        val timePairs = elementRegex.findAll(input).map {
            val quantity = it.groupValues[1].toDouble()
            val quantifier = it.groupValues[2].lowercase()
            TimePair(quantity, quantifier)
        }.toList()

        val unknownQuantifiers = timePairs.filter { it.quantifier !in quantifierValues.keys }.map { it.quantifier }

        if (unknownQuantifiers.isNotEmpty())
            return Error("Unknown quantifier: ${unknownQuantifiers.first()}")

        val timeInSeconds = timePairs.sumOf { it.seconds }
        return Success(timeInSeconds)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("1h15m5s")
}

private data class TimePair(val quantity: Double, val quantifier: String) {
    val seconds: Double
        get() = quantity * quantifierValues.getValue(quantifier)
}

private val fullRegex = Regex("^(\\d+[A-Za-z]+)+\$")
private val elementRegex = Regex("(\\d+)([A-Za-z]+)")

private val quantifierValues = listOf(
    1 to listOf("s", "sec", "second", "seconds"),
    60 to listOf("m", "min", "mins", "minute", "minutes"),
    3600 to listOf("h", "hr", "hrs", "hour", "hours"),
    86400 to listOf("d", "day", "days"),
    604800 to listOf("w", "week", "weeks"),
    2592000 to listOf("month", "months"),
    31536000 to listOf("y", "yr", "yrs", "year", "years")
).flatMap { (quantity, quantifiers) -> quantifiers.map { it to quantity } }.toMap()
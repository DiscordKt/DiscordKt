package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import kotlin.random.Random

/**
 * Accept a decimal number in the double range.
 */
public open class DoubleArg(override val name: String = "Double",
                            override val description: String = internalLocale.doubleArgDescription) : Argument<Double> {
    /**
     * Accept a decimal number in the double range.
     */
    public companion object : DoubleArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Double> =
        when (val result = arg.toDoubleOrNull()) {
            null -> Error(internalLocale.invalidFormat)
            else -> Success(result)
        }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("%.2f".format(Random.nextDouble(0.00, 9.99)))
}
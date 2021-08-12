package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import kotlin.random.Random

/**
 * Accept a decimal number in the double range.
 */
open class DoubleArg(override val name: String = "Double",
                     override val description: String = internalLocale.doubleArgDescription) : Argument<Double> {
    /**
     * Accept a decimal number in the double range.
     */
    companion object : DoubleArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toDoubleOrNull()) {
            null -> Error(internalLocale.invalidFormat)
            else -> Success(result)
        }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf("%.2f".format(Random.nextDouble(0.00, 9.99)))
}
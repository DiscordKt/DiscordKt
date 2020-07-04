package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import kotlin.random.Random

/**
 * Accept a decimal number in the double range.
 */
open class DoubleArg(override val name: String = "Double") : ArgumentType<Double>() {
    companion object : DoubleArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toDoubleOrNull()) {
            null -> Error<Double>("Couldn't parse $name from $arg.")
            else -> Success(result)
        }

    override fun generateExamples(event: CommandEvent<*>) = listOf("%.2f".format(Random.nextDouble(0.00, 9.99)))
}
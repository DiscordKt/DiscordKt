package me.jakejmattson.kutils.api.arguments


import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import kotlin.random.Random

/**
 * Accept a whole number in the int range.
 */
open class IntArg(override val name: String = "Int") : ArgumentType<Int>() {
    companion object : IntArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toIntOrNull()) {
            null -> Error<Int>("Couldn't parse $name from $arg.")
            else -> Success(result)
        }

    override fun generateExamples(event: CommandEvent<*>) = (0..10).map { it.toString() }
}

/**
 * Accept a whole number in the long range.
 */
open class LongArg(override val name: String = "Long") : ArgumentType<Long>() {
    companion object : LongArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toLongOrNull()) {
            null -> Error<Long>("Couldn't parse $name from $arg.")
            else -> Success(result)
        }

    override fun generateExamples(event: CommandEvent<*>) = (0..10).map { it.toString() }
}

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
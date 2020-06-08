package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import kotlin.random.Random

open class DoubleArg(override val name: String = "Decimal") : ArgumentType<Double>() {
    companion object : DoubleArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Double> {
        val double = arg.toDoubleOrNull()
            ?: return ArgumentResult.Error("Expected a decimal number, got $arg")

        return ArgumentResult.Success(double)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(
        "%.2f".format(Random.nextDouble(0.00, 9.99))
    )
}
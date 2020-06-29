package me.jakejmattson.kutils.internal.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.internal.arguments.ConversionResult.Success
import me.jakejmattson.kutils.internal.services.generateStructure

internal sealed class ConversionResult {
    data class Success(val results: List<Any>) : ConversionResult()
    data class Error(val error: String) : ConversionResult()
}

private fun convertOptional(arg: ArgumentType<*>, event: CommandEvent<*>) = arg.defaultValue?.invoke(event)

internal fun convertArguments(actual: List<String>, expected: List<ArgumentType<*>>, event: CommandEvent<*>): ConversionResult {
    val remainingArgs = actual.filter { it.isNotBlank() }.toMutableList()
    val expectation = "Expected: `${generateStructure(event.command!!)}`"

    val converted = expected.map { expectedArg ->
        if (remainingArgs.isEmpty()) {
            if (expectedArg.isOptional)
                convertOptional(expectedArg, event)
            else {
                val conversion = expectedArg.convert("", emptyList(), event)

                when (conversion) {
                    is Error -> return ConversionResult.Error("Missing input for `${expectedArg.name}`")
                    is me.jakejmattson.kutils.api.dsl.arguments.Success -> {
                        if (conversion.consumed != 0)
                            throw IllegalArgumentException("ArgumentType ${expectedArg.name} consumed more arguments than available.")

                        conversion.result
                    }
                }
            }
        } else {
            val firstArg = remainingArgs.first()
            val result = expectedArg.convert(firstArg, remainingArgs, event)

            when (result) {
                is me.jakejmattson.kutils.api.dsl.arguments.Success -> {
                    if (result.consumed > remainingArgs.size)
                        throw IllegalArgumentException("ArgumentType ${expectedArg.name} consumed more arguments than available.")
                    else
                        remainingArgs.subList(0, result.consumed).toList().forEach { remainingArgs.remove(it) }

                    result.result
                }
                is Error -> {
                    if (expectedArg.isOptional)
                        convertOptional(expectedArg, event)
                    else
                        return ConversionResult.Error(result.error)
                }
            }
        }
    }

    if (remainingArgs.isNotEmpty())
        return ConversionResult.Error("Received more arguments than expected. $expectation")

    return Success(converted as List<Any>)
}
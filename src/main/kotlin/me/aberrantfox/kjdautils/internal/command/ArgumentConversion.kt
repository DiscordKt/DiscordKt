package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.internal.command.Result.*

const val separatorCharacter = "|"

sealed class Result {
    data class Success(val results: List<Any>) : Result()
    data class Error(val error: String) : Result()
}

internal fun convertArguments(actual: List<String>, expected: List<ArgumentType<*>>, event: CommandEvent<*>): Result {
    return convertArgs(actual, expected, event)
}

private fun convertOptional(arg: ArgumentType<*>, event: CommandEvent<*>) = arg.defaultValue?.invoke(event)

private fun convertNoneConsumption(arg: ArgumentType<*>, event: CommandEvent<*>) =
    arg.convert("", listOf(""), event)

private fun convertArgs(actual: List<String>, expected: List<ArgumentType<*>>, event: CommandEvent<*>): Result {
    val remaining = actual.toMutableList()

    var lastError: ArgumentResult.Error<*>? = null

    val converted = expected.map { expectedArg ->
        if (remaining.isEmpty()) {
            when {
                expectedArg.isOptional -> convertOptional(expectedArg, event)
                expectedArg.consumptionType == ConsumptionType.None -> {
                    val result = convertNoneConsumption(expectedArg, event)

                    when (result) {
                        is ArgumentResult.Success -> result.result
                        is ArgumentResult.Error -> return Error(result.error)
                    }
                }
                else -> return Error("Missing non-optional argument(s). Try using `help`")
            }
        } else {
            var actualArg = remaining.first()

            if (expectedArg.consumptionType != ConsumptionType.None)
                while (actualArg.isBlank()) {
                    remaining.remove(actualArg)
                    actualArg = remaining.first()
                }

            val result = expectedArg.convert(actualArg, remaining.toList(), event)

            when (result) {
                is ArgumentResult.Success -> {
                    if (result.consumed.isNotEmpty())
                        result.consumed.forEach {
                            remaining.remove(it)
                        }
                    else
                        if (expectedArg.consumptionType != ConsumptionType.None)
                            remaining.remove(actualArg)

                    result.result
                }
                is ArgumentResult.Error -> {
                    lastError = result

                    if (expectedArg.isOptional)
                        convertOptional(expectedArg, event)
                    else
                        return Error(result.error)
                }
            }
        }
    }

    if (remaining.isNotEmpty())
        return Error(lastError?.error ?: "Unmatched arguments. Try using `help`")

    return Success(converted as List<Any>)
}

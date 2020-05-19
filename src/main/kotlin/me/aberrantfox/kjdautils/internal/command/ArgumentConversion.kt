package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.Result.*
import me.aberrantfox.kjdautils.internal.services.generateStructure

sealed class Result {
    data class Success(val results: List<Any>) : Result()
    data class Error(val error: String) : Result()
}

private fun convertOptional(arg: ArgumentType<*>, event: CommandEvent<*>) = arg.defaultValue?.invoke(event)

internal fun convertArguments(actual: List<String>, expected: List<ArgumentType<*>>, event: CommandEvent<*>): Result {
    val remainingArgs = actual.toMutableList().filter { it.isNotBlank() }.toMutableList()
    val expectation = "Expected: `${generateStructure(event.command!!)}`"

    val converted = expected.map { expectedArg ->
        if (remainingArgs.isEmpty()) {
            if (expectedArg.isOptional)
                convertOptional(expectedArg, event)
            else {
                val conversion = expectedArg.convert("", emptyList(), event)
                    .takeIf { it is ArgumentResult.Success && it.consumed == 0 }
                    ?: return Error("Received less arguments than expected. $expectation")

                (conversion as ArgumentResult.Success).result
            }
        }
        else {
            val firstArg = remainingArgs.first()
            val result = expectedArg.convert(firstArg, remainingArgs, event)

            when (result) {
                is ArgumentResult.Success -> {
                    if (result.consumed > remainingArgs.size) {
                        if (!expectedArg.isOptional)
                            return Error("Received less arguments than expected. $expectation")
                    }
                    else
                        remainingArgs.subList(0, result.consumed).toList().forEach { remainingArgs.remove(it) }

                    result.result
                }
                is ArgumentResult.Error -> {
                    if (expectedArg.isOptional)
                        convertOptional(expectedArg, event)
                    else
                        return Error(result.error)
                }
            }
        }
    }

    if (remainingArgs.isNotEmpty())
        return Error("Received more arguments than expected. $expectation")

    return Success(converted as List<Any>)
}

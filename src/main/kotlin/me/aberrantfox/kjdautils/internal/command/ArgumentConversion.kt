package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.internal.command.Result.*
import me.aberrantfox.kjdautils.internal.services.generateStructure

const val separatorCharacter = "|"

sealed class Result {
    data class Success(val results: List<Any>) : Result()
    data class Error(val error: String) : Result()
}

private fun convertOptional(arg: ArgumentType<*>, event: CommandEvent<*>) = arg.defaultValue?.invoke(event)

internal fun convertArguments(actual: List<String>, expected: List<ArgumentType<*>>, event: CommandEvent<*>): Result {
    val remaining = actual.toMutableList().filter { it.isNotBlank() }.toMutableList()

    val converted = expected.map { expectedArg ->
        val firstArg = remaining.firstOrNull() ?: ""
        val result = expectedArg.convert(firstArg, remaining, event)

        when (result) {
            is ArgumentResult.Success -> {
                if (result.consumed > remaining.size)
                    return Error("Ran out of arguments. Expected: `${generateStructure(event.command!!)}`")

                remaining.subList(0, result.consumed).toList().forEach { remaining.remove(it) }
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

    if (remaining.isNotEmpty())
        return Error("Received more arguments than expected. Try using `help`")

    return Success(converted as List<Any>)
}

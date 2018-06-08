package me.aberrantfox.kjdautils.internal.command


import me.aberrantfox.kjdautils.api.dsl.CommandArgument
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult.Multiple
import me.aberrantfox.kjdautils.internal.command.ArgumentResult.Single
import me.aberrantfox.kjdautils.internal.command.Result.Error
import me.aberrantfox.kjdautils.internal.command.Result.Results
import me.aberrantfox.kjdautils.internal.command.arguments.Manual

const val separatorCharacter = "|"

sealed class Result {
    fun then(function: (List<Any?>) -> Result): Result =
            when (this) {
                is Results -> function(results)
                is Error -> this
            }

    data class Results(val results: List<Any?>) : Result()
    data class Error(val error: String) : Result()
}

internal fun convertArguments(actual: List<String>, expected: List<CommandArgument>, event: CommandEvent): Result {

    val expectedTypes = expected.map { it.type }

    if (expectedTypes.contains(Manual)) {
        return Results(actual)
    }

    val result = convertArgs(actual, expected, event)
            .then { convertOptionalArgs(it, expected, event) }

    val converted = when (result) {
        is Results -> result.results
        is Error -> return result
    }

    val noUnfilledNonOptionals = converted
            .filterIndexed { i, arg -> arg == null && !expected[i].optional }
            .isEmpty()

    return when {
        noUnfilledNonOptionals -> result
        else -> Error("You did not fill all of the non-optional arguments.")
    }
}

private fun convertArgs(actual: List<String>, expected: List<CommandArgument>, event: CommandEvent): Result {

    val converted = arrayOfNulls<Any?>(expected.size)

    val remaining = actual.toMutableList()

    while (remaining.isNotEmpty()) {
        val actualArg = remaining.first()

        val nextMatchingIndex = expected.withIndex().indexOfFirst {
            it.value.type.isValid(actualArg, event) && converted[it.index] == null
        }
        if (nextMatchingIndex == -1) return Error("Couldn't match '$actualArg' with the expected arguments. Try using the `help` command.")

        val expectedArg = expected[nextMatchingIndex]
        val expectedType = expectedArg.type

        val result = expectedType.convert(actualArg, remaining.toList(), event)

        val convertedValue = when (result) {
            is Single -> {
                remaining.remove(actualArg)
                result.result
            }
            is Multiple -> {
                result.consumed.map { remaining.remove(it) }
                result.result
            }
            is ArgumentResult.Error -> {
                if (expectedArg.optional) {
                    val default = expectedArg.defaultValue

                    if (default is Function<*>) {
                        (default as (CommandEvent) -> Any).invoke(event)
                    } else {
                        default
                    }
                } else return Error(result.error)
            }
        }

        converted[nextMatchingIndex] = convertedValue
    }

    return Results(converted.toList())
}

private fun convertOptionalArgs(args: List<Any?>, expected: List<CommandArgument>, event: CommandEvent): Result {
    val zip = args.zip(expected)

    val converted =
            zip.map { (arg, expectedArg) ->
                if (arg != null || !expectedArg.optional) return@map arg

                val default = expectedArg.defaultValue

                return@map when (default) {
                    is Function<*> -> (default as (CommandEvent) -> Any).invoke(event)
                    else -> default
                }
            }

    return Results(converted)
}
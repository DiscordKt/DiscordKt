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

    fun thenIf(condition: Boolean, function: (List<Any?>) -> Result) =
            if (condition) {
                then(function)
            } else {
                this
            }

    data class Results(val results: List<Any?>) : Result()
    data class Error(val error: String) : Result()
}

fun convertArguments(actual: List<String>, expected: List<CommandArgument>, event: CommandEvent): Result {

    val expectedTypes = expected.map { it.type }

    if (expectedTypes.contains(Manual)) {
        return Results(actual)
    }

    return convertMainArgs(actual, expected, event)
            .then { convertOptionalArgs(it, expected, event) }
}

fun convertMainArgs(actual: List<String>, expected: List<CommandArgument>, event: CommandEvent): Result {

    val converted = arrayOfNulls<Any?>(expected.size)

    val remaining = actual.toMutableList()

    while (remaining.isNotEmpty()) {
        val actualArg = remaining.first()

        if (actualArg.isBlank()) {
            remaining.remove(actualArg)
            continue
        }

        val nextMatchingIndex = expected.withIndex().indexOfFirst {
            it.value.type.isValid(actualArg, event) && converted[it.index] == null
        }
        if (nextMatchingIndex == -1) return Error("Couldn't match '$actualArg' with the expected arguments. Try using the `help` command.")

        val expectedType = expected[nextMatchingIndex].type

        val result = expectedType.convert(actualArg, remaining.toList(), event)

        val convertedValue =
                when (result) {
                    is Single -> {
                        remaining.remove(actualArg)

                        result.result
                    }
                    is Multiple -> {
                        result.consumed.map {
                            remaining.remove(it)
                        }

                        result.result
                    }
                    is ArgumentResult.Error -> return Error(result.error)
                }

        converted[nextMatchingIndex] = convertedValue
    }

    val unfilledNonOptionals = converted.filterIndexed { i, arg -> arg == null && !expected[i].optional }

    if (unfilledNonOptionals.isNotEmpty())
        return Error("You did not fill all of the non-optional arguments.")

    return Results(converted.toList())
}

fun convertOptionalArgs(args: List<Any?>, expected: List<CommandArgument>, event: CommandEvent): Result {
    val zip = args.zip(expected)

    val converted =
            zip.map { (arg, expectedArg) ->
                if (arg != null) return@map arg

                val default = expectedArg.defaultValue

                if (default is Function<*>) {
                    return@map (default as (CommandEvent) -> Any).invoke(event)
                } else {
                    return@map default
                }
            }

    return Results(converted)
}

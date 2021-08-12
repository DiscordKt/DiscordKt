package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.api.TypeContainer
import me.jakejmattson.discordkt.api.arguments.Argument
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.arguments.Success
import me.jakejmattson.discordkt.api.bundleToContainer
import me.jakejmattson.discordkt.api.dsl.CommandEvent

/**
 * Intermediate result of manual parsing.
 */
internal interface ParseResult {
    data class Success(val argumentContainer: TypeContainer) : ParseResult
    data class Fail(val reason: String = "") : ParseResult
}

internal sealed class DataMap(val argument: Argument<Any>)
internal data class SuccessData<T>(private val arg: Argument<Any>, val value: T) : DataMap(arg)
internal data class ErrorData(private val arg: Argument<Any>, val error: String) : DataMap(arg)

internal suspend fun convertArguments(event: CommandEvent<*>, expected: List<Argument<*>>, actual: List<String>): ParseResult {
    val remainingArgs = actual.filter { it.isNotBlank() }.toMutableList()
    var hasFatalError = false

    expected as List<Argument<Any>>

    val conversionData = expected.map { expectedArg ->
        if (hasFatalError)
            return@map ErrorData(expectedArg, "")

        val firstArg = remainingArgs.firstOrNull() ?: ""

        when (val conversionResult = expectedArg.convert(firstArg, remainingArgs, event)) {
            is Success -> {
                if (conversionResult.consumed > remainingArgs.size)
                    throw IllegalArgumentException("Argument ${expectedArg.name} consumed more arguments than available.")

                if (remainingArgs.isNotEmpty())
                    remainingArgs.slice(0 until conversionResult.consumed).forEach { remainingArgs.remove(it) }

                SuccessData(expectedArg, conversionResult.result)
            }
            is Error -> {
                hasFatalError = true
                ErrorData(expectedArg, if (remainingArgs.isNotEmpty()) "<${conversionResult.error}>" else "<Missing>")
            }
        }
    }

    val error = formatDataMap(conversionData) +
        if (!hasFatalError && remainingArgs.isNotEmpty()) {
            hasFatalError = true
            "\n\nUnused: " + remainingArgs.joinToString(" ")
        } else ""

    if (hasFatalError)
        return ParseResult.Fail("```$error```")

    return ParseResult.Success(bundleToContainer(conversionData.filterIsInstance<SuccessData<*>>().map { it.value }))
}

private fun formatDataMap(successData: List<DataMap>): String {
    val length = successData.map { it.argument.name.length }.maxOrNull() ?: 0

    return successData.joinToString("\n") { data ->
        val arg = data.argument

        val value = when (data) {
            is SuccessData<*> -> data.value?.let { arg.formatData(it) }
            is ErrorData -> data.error
        }

        "%-${length}s = %s".format(arg.name, value)
    }
}
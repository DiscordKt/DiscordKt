package me.jakejmattson.discordkt.internal.arguments

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent

internal sealed class ConversionResult
internal data class ConversionSuccess(val results: List<Any?>) : ConversionResult()
internal data class ConversionError(val error: String) : ConversionResult()

internal sealed class DataMap(val argument: ArgumentType<Any>)
internal data class SuccessData<T>(private val arg: ArgumentType<Any>, val value: T) : DataMap(arg)
internal data class ErrorData(private val arg: ArgumentType<Any>, val error: String) : DataMap(arg)

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

internal suspend fun convertArguments(actual: List<String>, expected: List<ArgumentType<Any>>, event: CommandEvent<*>): ConversionResult {
    val remainingArgs = actual.filter { it.isNotBlank() }.toMutableList()
    var hasFatalError = false

    val conversionData = expected.map { expectedArg ->
        if (hasFatalError)
            return@map ErrorData(expectedArg, "")

        val firstArg = remainingArgs.firstOrNull() ?: ""

        when (val conversionResult = expectedArg.convert(firstArg, remainingArgs, event)) {
            is Success -> {
                if (conversionResult.consumed > remainingArgs.size)
                    throw IllegalArgumentException("ArgumentType ${expectedArg.name} consumed more arguments than available.")

                if (remainingArgs.isNotEmpty())
                    remainingArgs.slice(0 until conversionResult.consumed).forEach { remainingArgs.remove(it) }

                SuccessData(expectedArg, conversionResult.result)
            }
            is Error -> {
                if (expectedArg.isOptional)
                    SuccessData(expectedArg, expectedArg.defaultValue.invoke(event))
                else {
                    hasFatalError = true
                    ErrorData(expectedArg, if (remainingArgs.isNotEmpty()) "<${conversionResult.error}>" else "<Missing>")
                }
            }
        }
    }

    val error = formatDataMap(conversionData) +
        if (!hasFatalError && remainingArgs.isNotEmpty()) {
            hasFatalError = true
            "\n\nRemaining Args: " + remainingArgs.joinToString(" ")
        } else ""

    if (hasFatalError)
        return ConversionError("```$error```")

    return ConversionSuccess(conversionData.filterIsInstance<SuccessData<*>>().map { it.value })
}
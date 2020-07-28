package me.jakejmattson.kutils.internal.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

internal sealed class ConversionResult {
    data class Success(val results: List<Any>) : ConversionResult()
    data class Error(val error: String) : ConversionResult()
}

internal data class DataMap<T>(val argument: ArgumentType<Any>, val value: T)

private fun formatDataMap(data: List<DataMap<*>>): String {
    val length = data.maxBy { it.argument.name.length }?.argument?.name?.length ?: 0

    return data.joinToString("\n") {
        "%-${length}s = %s".format(it.argument.name, it.argument.formatData(it.value!!))
    }
}

internal fun convertArguments(actual: List<String>, expected: List<ArgumentType<Any>>, event: CommandEvent<*>): ConversionResult {
    val remainingArgs = actual.filter { it.isNotBlank() }.toMutableList()
    var hasFatalError = false

    val conversionData = expected.map { expectedArg ->
        if (hasFatalError)
            return@map DataMap(expectedArg, "")

        val conversionResult = if (remainingArgs.isNotEmpty())
            expectedArg.convert(remainingArgs.first(), remainingArgs, event)
        else
            expectedArg.convert("", emptyList(), event)

        when (conversionResult) {
            is Success -> {
                if (conversionResult.consumed > remainingArgs.size)
                    throw IllegalArgumentException("ArgumentType ${expectedArg.name} consumed more arguments than available.")

                if (remainingArgs.isNotEmpty())
                    remainingArgs.slice(0 until conversionResult.consumed).forEach { remainingArgs.remove(it) }

                DataMap(expectedArg, conversionResult.result)
            }
            is Error -> {
                if (expectedArg.isOptional)
                    DataMap(expectedArg, expectedArg.defaultValue?.invoke(event))
                else {
                    hasFatalError = true
                    DataMap(expectedArg, if (remainingArgs.isNotEmpty()) "<${conversionResult.error}>" else "<Missing>")
                }
            }
        }
    }

    val error = formatDataMap(conversionData) +
        if (!hasFatalError && remainingArgs.isNotEmpty()) {
            hasFatalError = true
            "\n\nRemaining Args: " + remainingArgs.joinToString()
        }
        else ""

    if (hasFatalError)
        return ConversionResult.Error("```$error```")

    val data = conversionData.map { it.value } as List<Any>

    return ConversionResult.Success(data)
}
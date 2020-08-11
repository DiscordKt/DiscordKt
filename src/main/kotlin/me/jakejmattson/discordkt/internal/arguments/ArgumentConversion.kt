package me.jakejmattson.discordkt.internal.arguments

import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent

internal sealed class ConversionResult
internal data class ConversionSuccess(val results: List<Any>) : ConversionResult()
internal data class ConversionError(val error: String) : ConversionResult()

internal sealed class DataMap(val argument: ArgumentType<Any>)
internal data class SuccessData<T>(private val arg: ArgumentType<Any>, val value: T) : DataMap(arg)
internal data class ErrorData(private val arg: ArgumentType<Any>, val error: String) : DataMap(arg)

private fun formatDataMap(successData: List<DataMap>): String {
    val length = successData.map { it.argument.name.length }.max() ?: 0

    return successData.joinToString("\n") {
        val arg = it.argument

        val value = when (it) {
            is SuccessData<*> -> arg.formatData(it.value!!)
            is ErrorData -> it.error
        }

        "%-${length}s = %s".format(arg.name, value)
    }
}

internal fun convertArguments(actual: List<String>, expected: List<ArgumentType<Any>>, event: CommandEvent<*>): ConversionResult {
    val remainingArgs = actual.filter { it.isNotBlank() }.toMutableList()
    var hasFatalError = false

    val conversionData = expected.map { expectedArg ->
        if (hasFatalError)
            return@map ErrorData(expectedArg, "")

        val firstArg = remainingArgs.firstOrNull() ?: ""
        val conversionResult = expectedArg.convert(firstArg, remainingArgs, event)

        when (conversionResult) {
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

    return ConversionSuccess(conversionData.map { (it as SuccessData<*>).value!! })
}
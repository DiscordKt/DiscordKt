package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.bundleToContainer
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Intermediate result of manual parsing.
 */
internal interface ParseResult {
    data class Success(val argumentContainer: TypeContainer) : ParseResult
    data class Fail(val reason: String = "") : ParseResult
}

internal sealed class DataMap(val argument: Argument<Any, Any>)
internal data class SuccessData<T>(private val arg: Argument<Any, Any>, val value: T) : DataMap(arg)
internal data class ErrorData(private val arg: Argument<Any, Any>, val error: String) : DataMap(arg)

internal suspend fun convertArguments(context: DiscordContext, expected: List<Argument<*, *>>, actual: List<String>): ParseResult {
    val remainingArgs = actual.filter { it.isNotBlank() }.toMutableList()
    var hasFatalError = false
    expected as List<Argument<Any, Any>>

    val conversionData = expected.map { expectedArg ->
        if (hasFatalError)
            return@map ErrorData(expectedArg, "")

        val conversionResult = expectedArg.parse(remainingArgs, context.discord)

        println("Expected: ${expectedArg.name}")
        println("Converted: $conversionResult")

        if (conversionResult != null) {
            val transformation = expectedArg.transform(conversionResult, context)

            println("Transformation: $transformation")

            if (transformation is Success)
                SuccessData(expectedArg, transformation.result)
            else
                ErrorData(expectedArg, (transformation as Error<*>).error)
        } else {
            hasFatalError = true
            ErrorData(expectedArg, if (remainingArgs.isNotEmpty()) "<${internalLocale.invalidFormat}>" else "<Missing>")
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
    val length = successData.maxOfOrNull { it.argument.name.length } ?: 0

    return successData.joinToString("\n") { data ->
        val arg = data.argument

        val value = when (data) {
            is SuccessData<*> -> data.value?.let { arg.formatData(it) }
            is ErrorData -> data.error
        }

        "%-${length}s = %s".format(arg.name, value)
    }
}
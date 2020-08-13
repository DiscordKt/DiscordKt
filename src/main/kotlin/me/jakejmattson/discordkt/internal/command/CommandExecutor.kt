package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.api.dsl.arguments.ArgumentType
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.internal.arguments.*
import me.jakejmattson.discordkt.internal.utils.InternalLogger

/**
 * Intermediate result of manual parsing.
 */
sealed class ParseResult {
    /**
     * The parsing succeeded.
     *
     * @param argumentContainer The parsing results.
     */
    data class Success(val argumentContainer: GenericContainer) : ParseResult()

    /**
     * The parsing failed.
     *
     * @param reason The reason for the failure.
     */
    data class Error(val reason: String) : ParseResult()
}

internal fun parseInputToBundle(command: Command, actualArgs: List<String>, event: CommandEvent<GenericContainer>): ParseResult {
    val expected = command.arguments
    val initialConversion = convertArguments(actualArgs, expected as List<ArgumentType<Any>>, event)

    val error = when (initialConversion) {
        is ConversionSuccess -> return ParseResult.Success(bundleToArgContainer(initialConversion.results))
        is ConversionError -> ParseResult.Error(initialConversion.error)
    }

    if (!command.isFlexible || expected.size < 2)
        return error

    val successList = expected
        .toMutableList()
        .generateAllPermutations()
        .map { it to convertArguments(actualArgs, it, event) }
        .filter { it.second is ConversionSuccess }
        .map { it.first to (it.second as ConversionSuccess).results }
        .map { (argumentTypes, results) -> argumentTypes.zip(results) }

    val success = when (successList.size) {
        0 -> return error
        1 -> successList.first()
        else -> {
            InternalLogger.error(
                """
                    Flexible command resolved ambiguously.
                    ${command.names.first()}(${expected.joinToString()})
                    Input: ${actualArgs.joinToString(" ")}
                """.trimIndent()
            )

            return error
        }
    }

    val orderedResult = expected.map { sortKey -> success.first { it.first == sortKey }.second }

    return ParseResult.Success(bundleToArgContainer(orderedResult))
}

private fun <E> MutableList<E>.generateAllPermutations(): List<List<E>> {
    if (isEmpty())
        return listOf(listOf())

    val firstElement = removeAt(0)
    val returnValue = mutableListOf<List<E>>()

    generateAllPermutations().forEachIndexed { index, list ->
        val temp = list.toMutableList()
        temp.add(index, firstElement)
        returnValue.add(temp)
    }

    return returnValue
}
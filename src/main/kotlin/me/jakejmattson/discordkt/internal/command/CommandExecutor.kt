package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.internal.arguments.*
import me.jakejmattson.discordkt.internal.utils.InternalLogger

/**
 * Intermediate result of manual parsing.
 */
internal interface ParseResult {
    /**
     * The parsing succeeded.
     *
     * @param argumentContainer The parsing results.
     */
    data class Success(val argumentContainer: TypeContainer) : ParseResult

    /**
     * Object indicating that an operation has failed.
     *
     * @param reason The reason for failure.
     */
    data class Fail(val reason: String = "") : ParseResult
}

internal suspend fun parseInputToBundle(command: Command, event: CommandEvent<*>, actualArgs: List<String>): ParseResult {
    val expected = command.arguments as List<ArgumentType<Any>>

    val error = when (val initialConversion = convertArguments(actualArgs, expected, event)) {
        is ConversionSuccess -> return ParseResult.Success(bundleToContainer(initialConversion.results))
        is ConversionError -> ParseResult.Fail(initialConversion.error)
    }

    if (!command.isFlexible || expected.size < 2)
        return error

    val successList = expected
        .toMutableList()
        .generatePermutations()
        .mapNotNull {
            when (val conversion = convertArguments(actualArgs, it, event)) {
                is ConversionSuccess -> it to conversion.results
                else -> null
            }
        }
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

    return ParseResult.Success(bundleToContainer(orderedResult))
}

private fun <E> MutableList<E>.generatePermutations(): List<List<E>> {
    if (isEmpty())
        return listOf(listOf())

    val firstElement = removeAt(0)
    val returnValue = mutableListOf<List<E>>()

    generatePermutations().forEach {
        (0..it.size).forEach { index ->
            val temp = it.toMutableList()
            temp.add(index, firstElement)
            returnValue.add(temp)
        }
    }

    return returnValue
}
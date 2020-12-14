package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.internal.arguments.*

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

internal suspend fun parseInputToBundle(execution: Execution<*>, event: CommandEvent<*>, actualArgs: List<String>): ParseResult {
    val expected = execution.arguments as List<ArgumentType<Any>>

    return when (val initialConversion = convertArguments(actualArgs, expected, event)) {
        is ConversionSuccess -> ParseResult.Success(bundleToContainer(initialConversion.results))
        is ConversionError -> ParseResult.Fail(initialConversion.error)
    }
}
package me.aberrantfox.kjdautils.internal.command


import me.aberrantfox.kjdautils.api.dsl.CommandArgument
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.extensions.jda.obtainRole
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.ConversionResult.Error
import me.aberrantfox.kjdautils.internal.command.ConversionResult.Results
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.TextChannel

const val separatorCharacter = "|"

val consumingArgTypes = listOf(ArgumentType.Sentence, ArgumentType.Splitter)
val multiplePartArgTypes = listOf(ArgumentType.Sentence, ArgumentType.Splitter, ArgumentType.TimeString)

enum class ArgumentType {
    Integer, Double, Word, Choice, Manual, Sentence, User,
    Splitter, URL, TimeString, TextChannel, VoiceChannel,
    Message, Role, Command
}

sealed class ConversionResult {
    fun then(function: (List<Any?>) -> ConversionResult): ConversionResult =
            when (this) {
                is Results -> function(results)
                is Error -> this
            }

    fun thenIf(condition: Boolean, function: (List<Any?>) -> ConversionResult) =
            if (condition) {
                then(function)
            } else {
                this
            }

    data class Results(val results: List<Any?>, val consumed: List<String>? = null) : ConversionResult()
    data class Error(val error: String) : ConversionResult()
}

fun convertArguments(actual: List<String>, expected: List<CommandArgument>, event: CommandEvent): ConversionResult {

    val expectedTypes = expected.map { it.type }

    if (expectedTypes.contains(ArgumentType.Manual)) {
        return Results(actual)
    }

    return convertMainArgs(actual, expected, event)
            .then {
                convertOptionalArgs(it, expected, event)
            }.thenIf(expectedTypes.contains(ArgumentType.Message)) {
                retrieveMessageArgs(it, expected)
            } // final and separate message conversion because dependent on text channel arg being converted already
}

fun convertMainArgs(actual: List<String>, expected: List<CommandArgument>, event: CommandEvent): ConversionResult {

    val converted = arrayOfNulls<Any?>(expected.size)

    val remaining = actual.toMutableList()

    while (remaining.isNotEmpty()) {
        val actualArg = remaining.first()

        val nextMatchingIndex = expected.withIndex().indexOfFirst {
            matchesArgType(actualArg, it.value.type, event.container) && converted[it.index] == null
        }
        if (nextMatchingIndex == -1) return Error("Couldn't match '$actualArg' with the expected arguments. Try using the `help` command.")

        val expectedType = expected[nextMatchingIndex].type

        val result = convertArg(actualArg, expectedType, remaining, event)

        when (result) {
            is Results -> {} // carry on with `Results`
            is Error -> return result
        }

        val convertedValue = result.results.first()

        result.consumed?.map {
            remaining.remove(it)
        }

        converted[nextMatchingIndex] = convertedValue
    }

    val unfilledNonOptionals = converted.filterIndexed { i, arg -> arg == null && !expected[i].optional }

    if (unfilledNonOptionals.isNotEmpty())
        return Error("You did not fill all of the non-optional arguments.")

    return Results(converted.toList())
}

fun convertOptionalArgs(args: List<Any?>, expected: List<CommandArgument>, event: CommandEvent): ConversionResult {
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

fun retrieveMessageArgs(args: List<Any?>, expected: List<CommandArgument>): ConversionResult {
    val channel = args.firstOrNull { it is TextChannel } as TextChannel?
            ?: throw IllegalArgumentException("Message arguments must be used with a TextChannel argument to be converted automatically")

    val converted = args.zip(expected).map { (arg, expectedArg) ->

        if (expectedArg.type != ArgumentType.Message) return@map arg

        val message =
                try {
                    channel.getMessageById(arg as String).complete()
                } catch (e: RuntimeException) {
                    null
                } ?: return Error("Couldn't retrieve message from given channel.")

        return@map message
    }

    return Results(converted)
}

private fun matchesArgType(arg: String, type: ArgumentType, container: CommandsContainer): Boolean {
    return when (type) {
        ArgumentType.Integer -> arg.isInteger()
        ArgumentType.Double -> arg.isDouble()
        ArgumentType.Choice -> arg.isBooleanValue()
        ArgumentType.URL -> arg.containsURl()
        ArgumentType.Command -> container.has(arg.toLowerCase())
        else -> true
    }
}

private fun convertArg(arg: String, type: ArgumentType, remaining: MutableList<String>, event: CommandEvent): ConversionResult {

    val jda = event.jda
    val trimmed = arg.trimToID()

    val tryRetrieve = { action: (JDA) -> Any? -> tryRetrieveSnowflake(jda, action) }

    val result: Any = when (type) {
        ArgumentType.Integer -> arg.toInt()
        ArgumentType.Double -> arg.toDouble()
        ArgumentType.Choice -> arg.toBooleanValue()
        ArgumentType.Sentence -> joinArgs(remaining)
        ArgumentType.Splitter -> splitArg(remaining)
        ArgumentType.TimeString -> convertTimeString(remaining)
        ArgumentType.Command -> event.container[arg.toLowerCase()]
        ArgumentType.User -> tryRetrieve { jda.retrieveUserById(trimmed).complete() }
        ArgumentType.TextChannel -> tryRetrieve { jda.getTextChannelById(trimmed) }
        ArgumentType.VoiceChannel -> tryRetrieve { jda.getVoiceChannelById(trimmed) }
        ArgumentType.Role -> tryRetrieve { jda.obtainRole(trimmed) }

        else -> arg
    } ?: return Error("Couldn't retrieve $type: $arg")

    if (result !is Results) {
        return if (type in consumingArgTypes) {
            Results(listOf(result), consumed = remaining.toList())
        } else {
            Results(listOf(result), consumed = listOf(arg))
        }
    }

    return result
}

private fun tryRetrieveSnowflake(jda: JDA, action: (JDA) -> Any?): Any? =
        try {
            action(jda)
        } catch (e: RuntimeException) {
            null
        }

private fun joinArgs(actual: List<String>) = actual.joinToString(" ")

private fun splitArg(actual: List<String>): List<String> {
    val joined = joinArgs(actual)

    if (!(joined.contains(separatorCharacter))) return listOf(joined)

    return joined.split(separatorCharacter).toList()
}

package me.aberrantfox.kjdautils.internal.command


import me.aberrantfox.kjdautils.api.dsl.CommandArgument
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.extensions.jda.obtainRole
import me.aberrantfox.kjdautils.extensions.stdlib.*
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.ISnowflake
import me.aberrantfox.kjdautils.internal.command.ConversionResult.*
import net.dv8tion.jda.core.entities.TextChannel

const val separatorCharacter = "|"

val snowflakeConversions = mapOf<ArgumentType, JDA.(String) -> ISnowflake?>(
        ArgumentType.User to { x -> retrieveUserById(x).complete() },
        ArgumentType.TextChannel to JDA::getTextChannelById,
        ArgumentType.VoiceChannel to JDA::getVoiceChannelById,
        ArgumentType.Role to JDA::obtainRole
)

val snowflakeArgTypes = snowflakeConversions.keys
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
        return ConversionResult.Results(actual)
    }

    return convertMainArgs(actual, expected, event.container)
            .thenIf(expectedTypes.any(snowflakeArgTypes::contains)) {
                retrieveSnowflakes(it, expected, event.jda)
            }.then {
                convertOptionalArgs(it, expected, event)
            }.thenIf(expectedTypes.contains(ArgumentType.Message)) {
                retrieveMessageArgs(it, expected)
            } // final and separate message conversion because dependent on text channel arg being converted already
}

fun convertMainArgs(actual: List<String>, expected: List<CommandArgument>, container: CommandsContainer): ConversionResult {
    val converted = arrayOfNulls<Any?>(expected.size)

    val remaining = actual.toMutableList()

    while (remaining.isNotEmpty()) {
        val actualArg = remaining.first()

        val nextMatchingIndex = expected.withIndex().indexOfFirst {
            matchesArgType(actualArg, it.value.type, container) && converted[it.index] == null
        }
        if (nextMatchingIndex == -1) return Error("Couldn't match '$actualArg' with the expected arguments. Try using the `help` command.")

        val expectedType = expected[nextMatchingIndex].type

        val result = convertArg(actualArg, expectedType, remaining, container)

        if (result is Error) return result

        val convertedValue =
                when (result) {
                    is Results -> result.results.first()
                    else -> result
                }

        consumeArgs(actualArg, expectedType, result, remaining)

        converted[nextMatchingIndex] = convertedValue
    }

    val unfilledNonOptionals = converted.filterIndexed { i, arg -> arg == null && !expected[i].optional }

    if (unfilledNonOptionals.isNotEmpty())
        return Error("You did not fill all of the non-optional arguments.")

    return Results(converted.toList())
}

fun retrieveSnowflakes(args: List<Any?>, expected: List<CommandArgument>, jda: JDA): ConversionResult {
    val converted =
            args.zip(expected).map { (arg, expectedArg) ->

                val conversionFun = snowflakeConversions[expectedArg.type]

                if (conversionFun == null || arg == null) return@map arg

                val retrieved =
                        try {
                            conversionFun(jda, (arg as String).trimToID())
                        } catch (e: RuntimeException) {
                            null
                        } ?: return Error("Couldn't retrieve ${expectedArg.type}: $arg.")

                return@map retrieved
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

fun convertOptionalArgs(args: List<Any?>, expected: List<CommandArgument>, event: CommandEvent) =
        args.zip(expected)
                .map { (arg, expectedArg) ->
                    arg ?: if (expectedArg.defaultValue is Function<*>)
                        (expectedArg.defaultValue as (CommandEvent) -> Any).invoke(event)
                    else
                        expectedArg.defaultValue
                }.let { Results(it) }


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


private fun convertArg(arg: String, type: ArgumentType, actual: MutableList<String>, container: CommandsContainer) =
        when (type) {
            ArgumentType.Integer -> arg.toInt()
            ArgumentType.Double -> arg.toDouble()
            ArgumentType.Choice -> arg.toBooleanValue()
            ArgumentType.Command -> container[arg.toLowerCase()] ?: throw IllegalStateException("Command argument should have been already verified as valid.")
            ArgumentType.Sentence -> joinArgs(actual)
            ArgumentType.Splitter -> splitArg(actual)
            ArgumentType.TimeString -> convertTimeString(actual)
            else -> arg
        }

private fun consumeArgs(actualArg: String, type: ArgumentType, result: Any, remaining: MutableList<String>) {
    if (type !in multiplePartArgTypes) {
        remaining.remove(actualArg)
    } else if (type in consumingArgTypes) {
        remaining.clear()
    }

    if (result is Results) {
        result.consumed?.map {
            remaining.remove(it)
        }
    }
}

private fun joinArgs(actual: List<String>) = actual.joinToString(" ")

private fun splitArg(actual: List<String>): List<String> {
    val joined = joinArgs(actual)

    if (!(joined.contains(separatorCharacter))) return listOf(joined)

    return joined.split(separatorCharacter).toList()
}

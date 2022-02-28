package me.jakejmattson.discordkt.internal.command

import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.bundleToContainer
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.internal.utils.stringify

@OptIn(KordPreview::class)
internal suspend fun transformArgs(args: List<Pair<Argument<*, *>, Any?>>, context: DiscordContext): Result<*> {
    val transformations = args.map { (rawArg, value) ->
        if (value == null) {
            require(rawArg is OptionalArg<*, *, *>) { "Missing required arguments" }
            runBlocking { Success(rawArg.default.invoke(context)) }
        } else {
            val arg = when (rawArg) {
                is OptionalArg<*, *, *> -> rawArg.type
                else -> rawArg
            }

            when (arg) {
                is MultipleArg<*, *> -> (arg as MultipleArg<Any, *>).transform(value as List<Any>, context)

                //Simple
                is StringArgument -> arg.transform(value as String, context)
                is IntegerArgument -> arg.transform(value as Int, context)
                is DoubleArgument -> arg.transform(value as Double, context)
                is BooleanArgument -> arg.transform(value as Boolean, context)

                //Entity
                is UserArgument -> arg.transform(value as User, context)
                is RoleArgument -> arg.transform(value as Role, context)
                is ChannelArgument -> arg.transform(value as Channel, context)
                is AttachmentArgument -> arg.transform(value as Attachment, context)

                //Unknown
                else -> Success(value)
            }
        }
    }

    return transformations.firstOrNull { it is Error }
        ?: Success(bundleToContainer(transformations.map { (it as Success<*>).result }))
}

internal suspend fun parseArguments(context: DiscordContext, expected: List<Argument<*, *>>, actual: List<String>): Result<List<Any?>> {
    val remainingArgs = actual.filter { it.isNotBlank() }.toMutableList()
    var hasFatalError = false
    expected as List<Argument<Any, Any>>

    val conversionData = expected.map { expectedArg ->
        if (hasFatalError)
            return@map ErrorData(expectedArg, "")

        val conversionResult = expectedArg.parse(remainingArgs, context.discord)

        if (conversionResult != null) {
            SuccessData(expectedArg, conversionResult)
        } else {
            if (expectedArg.isOptional())
                SuccessData(expectedArg, null)
            else {
                hasFatalError = true
                ErrorData(expectedArg, if (remainingArgs.isNotEmpty()) "<${internalLocale.invalidFormat}>" else "<Missing>")
            }
        }
    }

    val error = formatDataMap(conversionData) +
        if (!hasFatalError && remainingArgs.isNotEmpty()) {
            hasFatalError = true
            "\n\nUnused: " + remainingArgs.joinToString(" ")
        } else ""

    if (hasFatalError)
        return Error("```$error```")

    return Success(conversionData.filterIsInstance<SuccessData<*>>().map { it.value })
}

private fun formatDataMap(successData: List<DataMap>): String {
    val length = successData.maxOfOrNull { it.argument.name.length } ?: 0

    return successData.joinToString("\n") { data ->
        val value = when (data) {
            is SuccessData<*> -> data.value?.let { stringify(it) }
            is ErrorData -> data.error
        }

        "%-${length}s = %s".format(data.argument.name, value)
    }
}

internal sealed class DataMap(val argument: Argument<Any, Any>)
internal data class SuccessData<T>(private val arg: Argument<Any, Any>, val value: T) : DataMap(arg)
internal data class ErrorData(private val arg: Argument<Any, Any>, val error: String) : DataMap(arg)
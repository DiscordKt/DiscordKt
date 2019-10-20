package utilities

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*
import mock.commandEventMock

fun ArgumentType<*>.convertToSingle(
        arg: String,
        args: List<String> = listOf(arg),
        event: CommandEvent<*> = commandEventMock
) = (convert(arg, args, event) as ArgumentResult.Success).result

fun <T> ArgumentType<T>.attemptConvert(
        arg: String,
        args: List<String> = listOf(arg),
        event: CommandEvent<*> = commandEventMock
) = convert(arg, args, event)

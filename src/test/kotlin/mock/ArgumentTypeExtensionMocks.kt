package mock

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType

fun ArgumentType<*>.convertToSingle(
        arg: String,
        args: List<String> = listOf(arg),
        event: CommandEvent<*> = commandEventMock
) = (convert(arg, args, event) as ArgumentResult.Success).result

fun ArgumentType<*>.attemptConvert(
        arg: String,
        args: List<String> = listOf(arg),
        event: CommandEvent<*> = commandEventMock
) = convert(arg, args, event)

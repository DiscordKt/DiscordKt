package mock

import io.mockk.mockk
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType

fun ArgumentType.convertToSingle(
        arg: String,
        args: List<String> = listOf(arg),
        event: CommandEvent = mockk()
) = (convert(arg, args, event) as ArgumentResult.Single).result

fun ArgumentType.convertToError(
        arg: String,
        args: List<String> = listOf(arg),
        event: CommandEvent = mockk()
) = convert(arg, args, event) as ArgumentResult.Error

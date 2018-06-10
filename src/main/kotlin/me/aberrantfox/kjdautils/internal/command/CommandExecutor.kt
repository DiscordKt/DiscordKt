package me.aberrantfox.kjdautils.internal.command

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.Result.Error
import me.aberrantfox.kjdautils.internal.command.Result.Results

internal class CommandExecutor {

    fun executeCommand(command: Command, args: List<String>, event: CommandEvent) =
            launch(CommonPool) {
                invokeCommand(command, args, event)
            }

    private fun invokeCommand(command: Command, actualArgs: List<String>, event: CommandEvent) {
        val channel = event.channel

        getArgCountError(actualArgs, command)?.let {
            channel.sendMessage(it).queue()
            return
        }

        val conversionResult = convertArguments(actualArgs, command.expectedArgs.toList(), event)

        when (conversionResult) {
            is Results -> event.args = conversionResult.results.requireNoNulls()
            is Error -> {
                event.safeRespond(conversionResult.error)
                return
            }
        }

        command.execute(event)
    }
}


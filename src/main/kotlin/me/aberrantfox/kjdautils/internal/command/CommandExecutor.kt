package me.aberrantfox.kjdautils.internal.command

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.Result.Error
import me.aberrantfox.kjdautils.internal.command.Result.Results

internal class CommandExecutor {

    fun executeCommand(event: CommandEvent) =
            launch(CommonPool) {
                invokeCommand(event)
            }

    private fun invokeCommand(event: CommandEvent) {
        val channel = event.channel

        val actual = event.args as List<String>
        val command = event.command

        getArgCountError(actual, event.command)?.let {
            channel.sendMessage(it).queue()
            return
        }

        val conversionResult = convertArguments(actual, command.expectedArgs.toList(), event)

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


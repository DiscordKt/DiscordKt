package me.aberrantfox.kjdautils.internal.command

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import me.aberrantfox.kjdautils.api.Fail
import me.aberrantfox.kjdautils.api.Pass
import me.aberrantfox.kjdautils.api.PreconditionResult
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.KJDAConfiguration
import me.aberrantfox.kjdautils.internal.command.Result.Error
import me.aberrantfox.kjdautils.internal.command.Result.Results
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Message

internal class CommandExecutor(val container: CommandsContainer,
                               val jda: JDA,
                               val preconditions: ArrayList<(CommandEvent) -> PreconditionResult> = ArrayList()) {

    fun executeCommand(command: Command, actualArgs: List<String>, message: Message) =
            launch(CommonPool) {
                invokeCommand(command, actualArgs, message)
            }

    private fun invokeCommand(command: Command, actual: List<String>, message: Message) {
        val channel = message.channel

        getArgCountError(actual, command)?.let {
            channel.sendMessage(it).queue()
            return
        }

        val event = CommandEvent(command, message, actual, container)

        val failedPrecondition = preconditions
                .map { it.invoke(event) }
                .firstOrNull { it is Fail }

        if (failedPrecondition != null && failedPrecondition is Fail) {
            val reason = failedPrecondition.reason

            if (reason != null) {
                event.safeRespond(failedPrecondition.reason)
            }

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


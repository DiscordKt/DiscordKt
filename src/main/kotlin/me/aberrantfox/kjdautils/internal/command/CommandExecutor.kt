package me.aberrantfox.kjdautils.internal.command

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.KJDAConfiguration
import me.aberrantfox.kjdautils.internal.command.Result.Error
import me.aberrantfox.kjdautils.internal.command.Result.Results
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Message

internal class CommandExecutor(val config: KJDAConfiguration,
                               val container: CommandsContainer,
                               val jda: JDA,
                               val preconditions: ArrayList<(CommandEvent) -> Boolean> = ArrayList()) {

    fun executeCommand(command: Command, actualArgs: List<String>, message: Message) =
            launch(CommonPool) {
                invokeCommand(command, actualArgs, message)
            }

    private fun invokeCommand(command: Command, actual: List<String>, message: Message) {
        val channel = message.channel
        val author = message.author

        getArgCountError(actual, command)?.let {
            channel.sendMessage(it).queue()
            return
        }

        val event = CommandEvent(config, jda, channel, author, message, container, actual)
        if (!preconditions.all { it.invoke(event) }) return

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


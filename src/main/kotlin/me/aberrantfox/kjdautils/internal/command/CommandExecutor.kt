package me.aberrantfox.kjdautils.internal.command

import kotlinx.coroutines.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.*
import me.aberrantfox.kjdautils.internal.command.Result.Error

internal class CommandExecutor {

    fun executeCommand(command: Command, args: List<String>, event: CommandEvent<ArgumentCollection<*>>) = runBlocking {
        GlobalScope.launch {
            invokeCommand(command, args, event)
        }
    }

    private fun invokeCommand(command: Command, actualArgs: List<String>, event: CommandEvent<ArgumentCollection<*>>) {
        val channel = event.channel

        getArgCountError(actualArgs, command)?.let {
            if(event.discord.configuration.deleteErrors) channel.messageTimed(it)
            else channel.message(it)
            return
        }

        val conversionResult = convertArguments(actualArgs, command.expectedArgs, event)

        if (conversionResult is Error) {
            if(event.discord.configuration.deleteErrors) event.respondTimed(conversionResult.error)
            else event.respond(conversionResult.error)
            return
        }

        conversionResult as Result.Results

        //val event = CommandEvent()

        //TODO("Make KUtils work")
        //command.execute(conversionResult)
    }
}

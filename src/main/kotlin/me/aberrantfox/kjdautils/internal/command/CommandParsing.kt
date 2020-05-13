package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.internal.arguments.Manual

data class CommandStruct(val commandName: String,
                         val commandArgs: List<String> = listOf(),
                         val doubleInvocation: Boolean)

fun stripPrefixInvocation(message: String, config: KConfiguration): CommandStruct {
    val doubleInvocation = message.startsWith(config.prefix + config.prefix)
    val prefix = if (doubleInvocation) config.prefix + config.prefix else config.prefix
    val trimmedMessage = message.substringAfter(prefix)

    return produceCommandStruct(trimmedMessage, doubleInvocation)
}

fun stripMentionInvocation(message: String): CommandStruct {
    val trimmedMessage = message.substringAfter(">").trimStart()
    return produceCommandStruct(trimmedMessage)
}

private fun produceCommandStruct(message: String, doubleInvocation: Boolean = false): CommandStruct {
    if (!message.contains(" ")) {
        return CommandStruct(message.toLowerCase(), listOf(), doubleInvocation)
    }

    val commandName = message.substring(0, message.indexOf(" ")).toLowerCase()
    val commandArgs = message.substring(message.indexOf(" ") + 1).split(" ")

    return CommandStruct(commandName, commandArgs, doubleInvocation)
}
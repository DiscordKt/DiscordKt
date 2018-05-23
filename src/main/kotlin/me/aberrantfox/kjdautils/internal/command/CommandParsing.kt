package me.aberrantfox.kjdautils.internal.command


import me.aberrantfox.kjdautils.api.dsl.KJDAConfiguration
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.arg

data class CommandStruct(val commandName: String, val commandArgs: List<String> = listOf())

fun cleanCommandMessage(message: String, config: KJDAConfiguration): CommandStruct {
    var trimmedMessage = message.substring(config.prefix.length)

    if (trimmedMessage.startsWith(config.prefix)) trimmedMessage = trimmedMessage.substring(config.prefix.length)

    if (!message.contains(" ")) {
        return CommandStruct(trimmedMessage.toLowerCase())
    }

    val commandName = trimmedMessage.substring(0, trimmedMessage.indexOf(" ")).toLowerCase()
    val commandArgs = trimmedMessage.substring(trimmedMessage.indexOf(" ") + 1).split(" ")

    return CommandStruct(commandName, commandArgs)
}

fun getArgCountError(actual: List<String>, cmd: Command): String? {
    val optionalCount = cmd.expectedArgs.count { it.optional }
    val argCountRange = cmd.parameterCount - optionalCount..cmd.parameterCount

    if (cmd.expectedArgs.any { it.type in multiplePartArgTypes }) {
        if (actual.size < argCountRange.start) {
            return "You didn't enter the minimum number of required arguments: ${cmd.expectedArgs.size - optionalCount}."
        }
    } else {
        if (actual.size !in argCountRange && !cmd.expectedArgs.contains(arg(Manual))) {
            return "This command requires at least ${argCountRange.start} and a maximum of ${argCountRange.endInclusive} arguments."
        }
    }

    return null
}
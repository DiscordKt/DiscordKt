package me.aberrantfox.kjdautils.internal.command


import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.internal.arguments.Manual

data class CommandStruct(val commandName: String,
                         val commandArgs: List<String> = listOf(),
                         val doubleInvocation: Boolean)

fun cleanCommandMessage(message: String, config: KConfiguration): CommandStruct {
    var trimmedMessage = message.substring(config.prefix.length)
    val doubleInvocation = trimmedMessage.startsWith(config.prefix)

    if (doubleInvocation) {
        trimmedMessage = trimmedMessage.substring(config.prefix.length)
    }

    if (!message.contains(" ")) {
        return CommandStruct(trimmedMessage.toLowerCase(), listOf(), doubleInvocation)
    }

    val commandName = trimmedMessage.substring(0, trimmedMessage.indexOf(" ")).toLowerCase()
    val commandArgs = trimmedMessage.substring(trimmedMessage.indexOf(" ") + 1).split(" ")

    return CommandStruct(commandName, commandArgs, doubleInvocation)
}

fun getArgCountError(actual: List<String>, cmd: Command): String? {
    val optionalCount = cmd.expectedArgs.count { it.optional }
    val validRange = (cmd.parameterCount - optionalCount) .. cmd.parameterCount
    val actualNonBlank = actual.count { it.isNotBlank() }

    val manual = cmd.expectedArgs
            .map { it.type }
            .any { it == Manual }

    if (manual) return null

    val hasMultipleArg = cmd.expectedArgs
            .map { it.type.consumptionType }
            .any { it in listOf(ConsumptionType.Multiple, ConsumptionType.All) }

    if (hasMultipleArg) {
        if (actualNonBlank < validRange.start) {
            return "This command requires at least ${validRange.start} argument(s)"
        }
    } else if (actualNonBlank !in validRange) {
        return if (validRange.start == validRange.endInclusive) {
            "This command requires ${validRange.start} argument(s)."
        } else {
            "This command requires between ${validRange.start} and ${validRange.endInclusive} arguments."
        }
    }

    return null
}
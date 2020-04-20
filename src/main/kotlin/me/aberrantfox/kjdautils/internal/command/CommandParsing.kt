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

fun getArgCountError(actual: List<String>, cmd: Command): String? {
    val optionalCount = cmd.expectedArgs.arguments.count { it.isOptional }
    val noConsumptionCount = cmd.expectedArgs.arguments.count { it.consumptionType == ConsumptionType.None }
    val validRange = (cmd.parameterCount - optionalCount - noConsumptionCount)..cmd.parameterCount
    val actualNonBlank = actual.count { it.isNotBlank() }

    val manual = cmd.expectedArgs.arguments.any { it == Manual }
    if (manual) return null

    val hasMultipleArg = cmd.expectedArgs.arguments
        .map { it.consumptionType }
        .any { it in listOf(ConsumptionType.Multiple, ConsumptionType.All) }

    if (hasMultipleArg) {
        if (actualNonBlank < validRange.first) {
            return "This command requires at least ${validRange.first} argument(s)"
        }
    } else if (actualNonBlank !in validRange) {
        return if (validRange.first == validRange.last) {
            "This command requires ${validRange.first} argument(s)."
        } else {
            "This command requires between ${validRange.first} and ${validRange.last} arguments."
        }
    }

    return null
}
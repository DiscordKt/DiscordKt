package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.commands.RawInputs

internal fun stripPrefixInvocation(message: String, prefix: String): RawInputs {
    val prefixSeq = generateSequence(prefix) { it + prefix }
    val prefixBlock = prefixSeq.takeWhile { message.startsWith(it) }.last()
    val trimmed = message.removePrefix(prefixBlock)
    val invocationCount = (message.length - trimmed.length) / prefix.length

    return produceCommandStruct(message, trimmed, invocationCount)
}

internal fun stripMentionInvocation(message: String): RawInputs {
    val trimmedMessage = message.substringAfter(">").trimStart()
    return produceCommandStruct(message, trimmedMessage)
}

private fun produceCommandStruct(raw: String, message: String, invocationCount: Int = 1): RawInputs {
    if (!message.contains(" ")) {
        return RawInputs(raw, message.lowercase(), invocationCount, listOf())
    }

    val commandName = message.substring(0, message.indexOf(" ")).lowercase()
    val commandArgs = message.substring(message.indexOf(" ") + 1).split(" ")

    return RawInputs(raw, commandName, invocationCount, commandArgs)
}
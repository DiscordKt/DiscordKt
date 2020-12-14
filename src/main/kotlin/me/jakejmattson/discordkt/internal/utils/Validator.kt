package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.arguments.EitherArg
import me.jakejmattson.discordkt.api.dsl.Command

@PublishedApi
internal object Validator {
    fun validateCommandMeta(commands: MutableList<Command>) {
        val duplicates = commands
            .flatMap { it.names }
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
            .map { it.key }
            .joinToString { "\"$it\"" }

        if (duplicates.isNotEmpty())
            InternalLogger.error("Found commands with duplicate names: $duplicates")

        commands.forEach { command ->
            val commandName = command.names.first()

            if (command.names.any { it.isBlank() })
                InternalLogger.error("Found command with blank name in CommandSet ${command.category}")
            else {
                val spaces = command.names.filter { " " in it }

                if (spaces.isNotEmpty())
                    InternalLogger.error("Found command name with spaces: ${spaces.joinToString { "\"$it\"" }}")
            }

            command.executions.forEach {
                val args = it.arguments

                args.filterIsInstance<EitherArg<*, *>>().forEach {
                    if (it.left == it.right) {
                        val arg = it.left::class.simplerName
                        InternalLogger.error("Detected EitherArg with identical args ($arg) in command: $commandName")
                    }
                }
            }
        }
    }
}
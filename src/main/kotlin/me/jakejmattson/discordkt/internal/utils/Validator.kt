package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.arguments.EitherArg
import me.jakejmattson.discordkt.api.dsl.Command
import me.jakejmattson.discordkt.api.dsl.GlobalSlashCommand

@PublishedApi
internal object Validator {
    fun validateArgumentTypes(commands: MutableList<Command>) {
        commands.forEach { command ->
            command.executions.forEach { execution ->
                execution.arguments.forEach {
                    if (" " in it.name)
                        InternalLogger.error("[${command.category}-${command.names.first()}]: ${it.toSimpleString()}(\"${it.name}\") contains a space.")
                }
            }
        }
    }

    fun validateCommands(commands: MutableList<Command>) {
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

            if (command.executions.isEmpty())
                InternalLogger.error("$commandName has no execute block.")

            command.executions.forEach {
                val args = it.arguments

                args.filterIsInstance<EitherArg<*, *>>().forEach { eitherArg ->
                    if (eitherArg.left == eitherArg.right) {
                        val arg = eitherArg.left::class.simplerName
                        InternalLogger.error("Detected EitherArg with identical args ($arg) in command: $commandName")
                    }
                }
            }

            if (command is GlobalSlashCommand && command.executions.size > 1)
                InternalLogger.error("Slash commands ($commandName) cannot be overloaded.")
        }
    }
}
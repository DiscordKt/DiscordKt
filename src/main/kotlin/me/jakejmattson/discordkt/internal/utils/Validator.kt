package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.arguments.EitherArg
import me.jakejmattson.discordkt.api.dsl.Command
import me.jakejmattson.discordkt.api.dsl.GlobalSlashCommand
import me.jakejmattson.discordkt.api.dsl.PermissionSet

@PublishedApi
internal object Validator {
    fun validateArguments(commands: List<Command>) {
        commands.forEach { command ->
            command.executions.forEach { execution ->
                val commandName = command.names.first()

                if (command.executions.isEmpty())
                    InternalLogger.error("$commandName has no execute block.")

                execution.arguments.forEach {
                    if (" " in it.name)
                        InternalLogger.error("[${command.category}-${command.names.first()}]: ${it.toSimpleString()}(\"${it.name}\") contains a space.")
                }

                execution.arguments.filterIsInstance<EitherArg<*, *>>().forEach { eitherArg ->
                    if (eitherArg.left == eitherArg.right) {
                        val arg = eitherArg.left::class.simplerName
                        InternalLogger.error("Detected EitherArg with identical args ($arg) in command: $commandName")
                    }
                }
            }
        }
    }

    fun validateCommands(commands: List<Command>) {
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

            if (command is GlobalSlashCommand && command.executions.size > 1)
                InternalLogger.error("Slash commands ($commandName) cannot be overloaded.")
        }
    }

    fun validatePermissions(commands: List<Command>, discord: Discord) {
        val defaultRequiredPermission = discord.permissions.commandDefault
        val validPermissions = discord.permissions.levels

        if (defaultRequiredPermission !is PermissionSet)
            InternalLogger.fatalError("Permissions enum must extend ${PermissionSet::class.qualifiedName}")

        commands.forEach { command ->
            val requiredPermission = command.requiredPermission

            if (requiredPermission !in validPermissions)
                InternalLogger.error("${requiredPermission::class.simplerName}.${requiredPermission.name} provided to command " +
                    "(${command.category} - ${command.names.first()}) did not match expected type ${defaultRequiredPermission::class.simplerName}")
        }
    }
}
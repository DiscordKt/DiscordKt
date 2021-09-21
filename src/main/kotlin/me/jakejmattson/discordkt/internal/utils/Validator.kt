package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.arguments.Argument
import me.jakejmattson.discordkt.api.commands.MessageCommand
import me.jakejmattson.discordkt.api.commands.SlashCommand
import me.jakejmattson.discordkt.api.dsl.PermissionSet

private val slashRegex = "^[\\w-]{1,32}$".toRegex()

private fun Discord.validatePermissions() {
    val defaultRequiredPermission = permissions.commandDefault
    val validPermissions = permissions.levels

    if (defaultRequiredPermission !is PermissionSet)
        InternalLogger.fatalError("Permissions enum must extend ${PermissionSet::class.qualifiedName}")

    commands.forEach { command ->
        val requiredPermission = command.requiredPermission

        if (requiredPermission !in validPermissions)
            InternalLogger.error("${requiredPermission::class.simplerName}.${requiredPermission.name} provided to command " +
                "(${command.category} - ${command.names.first()}) did not match expected type ${defaultRequiredPermission::class.simplerName}")
    }
}

internal fun Discord.validate() {
    val duplicates = commands
        .flatMap { it.names }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()
        .filter { it.value > 1 }
        .map { it.key }
        .joinToString { "\"$it\"" }

    if (duplicates.isNotEmpty())
        InternalLogger.error("Found commands with duplicate names: $duplicates")

    val errors = Errors()

    commands.forEach { command ->
        with(command) {
            if (names.any { it.isBlank() })
                errors.blankCmdName.add(category)

            if (executions.isEmpty()) {
                errors.noExecution.add("$category-$name")
                return@forEach
            }

            when (this) {
                is SlashCommand -> {
                    if (executions.size > 1)
                        errors.slashMultiExec.add("$category-$name")

                    if (!name.matches(slashRegex))
                        errors.badRegexSlashCmd.add(this)

                    errors.badRegexSlashArg.addAll(execution
                        .arguments
                        .filter { !it.name.matches(slashRegex) }
                        .map { this to it }
                    )
                }
                is MessageCommand -> {
                    errors.spaceMsgCmd.addAll(names.filter { it.contains(" ") })

                    errors.spaceMsgArg.addAll(executions.flatMap { execution ->
                        execution.arguments.filter { " " in it.name }.map { this to it }
                    })
                }
            }
        }
    }

    errors.display()
    validatePermissions()
}

private data class Errors(
    //Execution
    val noExecution: MutableList<String> = mutableListOf(),
    val slashMultiExec: MutableList<String> = mutableListOf(),

    //Naming
    val blankCmdName: MutableSet<String> = mutableSetOf(),
    val spaceMsgCmd: MutableList<String> = mutableListOf(),
    val spaceMsgArg: MutableList<Pair<MessageCommand, Argument<*>>> = mutableListOf(),
    val badRegexSlashCmd: MutableList<SlashCommand> = mutableListOf(),
    val badRegexSlashArg: MutableList<Pair<SlashCommand, Argument<*>>> = mutableListOf()
) {
    private val indent = "  "

    private fun String.toIndicator() = map { if (slashRegex.matches(it.toString())) ' ' else '^' }.joinToString("")

    private fun StringBuilder.appendError(list: List<String>, message: String) {
        if (list.isNotEmpty())
            appendLine("$message: \n${list.joinToString("\n") { "$indent$it" }}\n")
    }

    fun display() {
        val fatalErrors = buildString {
            appendError(noExecution, "Commands must have at least one execute block")
            appendError(slashMultiExec, "Slash commands cannot have multiple execute blocks")
            appendError(blankCmdName.toList(), "Command names cannot be blank")
            appendError(spaceMsgCmd, "Command names cannot have spaces")

            if (badRegexSlashCmd.isNotEmpty()) {
                appendLine("Slash command names must follow regex ${slashRegex.pattern}")
                appendLine(badRegexSlashCmd.joinToString("\n") { cmd ->
                    val base = "$indent${cmd.category}-"
                    "$base${cmd.name}\n${" ".repeat(base.length)}${cmd.name.toIndicator()}"
                } + "\n")
            }

            if (badRegexSlashArg.isNotEmpty()) {
                appendLine("Slash argument names must follow regex ${slashRegex.pattern}")
                appendLine(badRegexSlashArg.joinToString("\n") { (cmd, arg) ->
                    val base = "$indent${cmd.category}-${cmd.name}-${arg::class.simplerName}(\""
                    "$base${arg.name}\")\n${" ".repeat(base.length)}${cmd.name.toIndicator()}"
                } + "\n")
            }
        }

        if (spaceMsgArg.isNotEmpty())
            InternalLogger.error("Arguments with spaces are not recommended:\n" +
                spaceMsgArg.joinToString("\n") { (cmd, arg) ->
                    "$indent${cmd.category}-${cmd.name}-${arg::class.simplerName}(\"${arg.name}\")"
                } + "\n"
            )

        if (fatalErrors.isNotEmpty())
            InternalLogger.fatalError("Invalid command configuration:\n${fatalErrors.lines().joinToString("\n") { "$indent$it" }}")
    }
}
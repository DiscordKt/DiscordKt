package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.commands.SlashCommand
import me.jakejmattson.discordkt.util.DiscordRegex

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

                    if (!name.matches(DiscordRegex.slashName))
                        errors.badRegexSlashCmd.add(this)

                    errors.badRegexSlashArg.addAll(execution
                        .arguments
                        .filter { !it.name.matches(DiscordRegex.slashName) }
                        .map { this to it }
                    )
                }
            }
        }
    }

    errors.display()
}

private data class Errors(
    //Execution
    val noExecution: MutableList<String> = mutableListOf(),
    val slashMultiExec: MutableList<String> = mutableListOf(),

    //Naming
    val blankCmdName: MutableSet<String> = mutableSetOf(),
    val spaceTxtCmd: MutableList<String> = mutableListOf(),
    val badRegexSlashCmd: MutableList<SlashCommand> = mutableListOf(),
    val badRegexSlashArg: MutableList<Pair<SlashCommand, Argument<*, *>>> = mutableListOf()
) {
    private val indent = "  "

    private fun String.toIndicator() = map { if (DiscordRegex.slashName.matches(it.toString())) ' ' else '^' }.joinToString("")

    private fun StringBuilder.appendError(list: List<String>, message: String) {
        if (list.isNotEmpty())
            appendLine("$message: \n${list.joinToString("\n") { "$indent$it" }}\n")
    }

    fun display() {
        val fatalErrors = buildString {
            appendError(noExecution, "Commands must have at least one execute block")
            appendError(slashMultiExec, "Slash commands cannot have multiple execute blocks")
            appendError(blankCmdName.toList(), "Command names cannot be blank")
            appendError(spaceTxtCmd, "Command names cannot have spaces")

            if (badRegexSlashCmd.isNotEmpty()) {
                appendLine("Slash command names must follow regex ${DiscordRegex.slashName.pattern}")
                appendLine(badRegexSlashCmd.joinToString("\n") { cmd ->
                    val base = "$indent${cmd.category}-"
                    "$base${cmd.name}\n${" ".repeat(base.length)}${cmd.name.toIndicator()}"
                } + "\n")
            }

            if (badRegexSlashArg.isNotEmpty()) {
                appendLine("Slash argument names must follow regex ${DiscordRegex.slashName.pattern}")
                appendLine(badRegexSlashArg.joinToString("\n") { (cmd, arg) ->
                    val base = "$indent${cmd.category}-${cmd.name}-${arg::class.simplerName}(\""
                    "$base${arg.name}\")\n${" ".repeat(base.length)}${cmd.name.toIndicator()}"
                } + "\n")
            }
        }

        if (fatalErrors.isNotEmpty())
            InternalLogger.fatalError("Invalid command configuration:\n${fatalErrors.lines().joinToString("\n") { "$indent$it" }}")
    }
}
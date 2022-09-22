package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.commands.Command
import me.jakejmattson.discordkt.util.DiscordRegex

internal fun Discord.validate() {
    val duplicates = commands
        .map { it.name }
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
            if (!name.matches(DiscordRegex.slashName))
                errors.badRegexSlashCmd.add(this)

            errors.badRegexSlashArg.addAll(execution.arguments
                .filter { !it.name.matches(DiscordRegex.slashName) }
                .map { this to it }
            )
        }
    }

    errors.display()
}

private data class Errors(
    val badRegexSlashCmd: MutableList<Command> = mutableListOf(),
    val badRegexSlashArg: MutableList<Pair<Command, Argument<*, *>>> = mutableListOf()
) {
    private val indent = "  "

    private fun String.toIndicator() = map { if (DiscordRegex.slashName.matches(it.toString())) ' ' else '^' }.joinToString("")

    fun display() {
        val fatalErrors = buildString {

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
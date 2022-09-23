package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.commands.Command
import me.jakejmattson.discordkt.commands.SubCommandSet
import java.io.File
import kotlin.math.max

internal fun createDocumentation(commands: List<Command>, subcommands: MutableList<SubCommandSet>) {
    if (commands.isEmpty() && subcommands.isEmpty())
        return

    data class CommandData(val name: String, val args: List<String>, val desc: String) {
        val longestArg
            get() = args.maxByOrNull { it.length } ?: ""

        fun format(format: String) = args
            .sortedBy { it.length }
            .mapIndexed { index: Int, value: String ->
                format.format(
                    if (index == 0) name else "",
                    value,
                    if (index == 0) desc else ""
                )
            }.joinToString("\n")
    }

    fun String.sanitizePipe() = replace("|", "\\|")
    fun List<CommandData>.maxLength(field: (CommandData) -> String) = maxOf { field.invoke(it).length }

    fun extractCommandData(command: Command): CommandData {
        val nameString = command.name.sanitizePipe()

        val expectedArgs = listOf(command.execution.arguments
            .joinToString { arg -> if (arg.isOptional()) "[${arg.name}]" else arg.name }
            .sanitizePipe())

        return CommandData(nameString, expectedArgs, command.description.sanitizePipe())
    }

    fun formatDocs(commandData: List<CommandData>): String {
        val header = CommandData("Commands", listOf("Arguments"), "Description")
        val longestName = max(commandData.maxLength { it.name }, header.name.length)
        val longestArgs = max(commandData.maxLength { it.longestArg }, header.args.first().length)
        val longestDesc = max(commandData.maxLength { it.desc }, header.desc.length)
        val formatString = "| %-${longestName}s | %-${longestArgs}s | %-${longestDesc}s |"

        fun divider(length: Int) = "-".repeat(length + 2)

        val headerString = header.format(formatString)
        val separator = "|${divider(longestName)}|${divider(longestArgs)}|${divider(longestDesc)}|"
        val commandString = commandData.sortedBy { it.name }.joinToString("\n") { it.format(formatString) }

        return "$headerString\n$separator\n$commandString\n"
    }

    val keyString = buildString {
        val argumentSet = commands.flatMap { cmd -> cmd.execution.arguments }.toSet()

        if (argumentSet.any { it.isOptional() })
            appendLine("| [Argument]  | Argument is not required.      |")

        if (subcommands.isNotEmpty())
            appendLine("| /Category   | This is a subcommand group.    |")
    }

    val key =
        if (keyString.isNotBlank())
            """
                ## Key 
                | Symbol      | Meaning                        |
                |-------------|--------------------------------|
            """.trimIndent() + "\n$keyString\n"
        else
            ""

    val groups =
        commands.groupBy { it.category }.map { it.key to formatDocs(it.value.map { cmd -> extractCommandData(cmd) }) } +
            subcommands.map { "/${it.name}" to formatDocs(it.commands.map { cmd -> extractCommandData(cmd) }) }

    val docs = groups
        .sortedBy { it.first }
        .joinToString("") { "## ${it.first}\n${it.second}\n" }

    File("commands.md").writeText("# Commands\n\n$key$docs")
}
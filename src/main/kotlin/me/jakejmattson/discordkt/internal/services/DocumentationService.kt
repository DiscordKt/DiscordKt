package me.jakejmattson.discordkt.internal.services

import me.jakejmattson.discordkt.api.arguments.MultipleArg
import me.jakejmattson.discordkt.api.dsl.command.Command
import java.io.File

internal fun createDocumentation(commands: List<Command>) {
    if (commands.isEmpty())
        return

    data class CommandData(val name: String, val args: String, val desc: String) {
        fun format(format: String) = String.format(format, name, args, desc)
    }

    fun String.sanitizePipe() = replace("|", "\\|")
    fun List<CommandData>.maxLength(header: String, field: (CommandData) -> String) = (map { field.invoke(it).length } + header.length).maxOrNull()!!

    fun extractCommandData(command: Command): CommandData {
        val nameString = (if (command.isFlexible) "*" else "") + command.names.joinToString().sanitizePipe()

        val expectedArgs = command.arguments.joinToString {
            if (it.isOptional) "(${it.name})" else it.name
        }.takeIf { it.isNotEmpty() } ?: ""

        return CommandData(nameString, expectedArgs.sanitizePipe(), command.description.sanitizePipe())
    }

    fun formatDocs(commandData: List<CommandData>): String {
        val header = CommandData("Commands", "Arguments", "Description")
        val longestName = commandData.maxLength(header.name) { it.name }
        val longestArgs = commandData.maxLength(header.args) { it.args }
        val longestDesc = commandData.maxLength(header.desc) { it.desc }
        val formatString = "| %-${longestName}s | %-${longestArgs}s | %-${longestDesc}s |"

        val headerString = header.format(formatString)
        val separator = formatString.format("-".repeat(longestName), "-".repeat(longestArgs), "-".repeat(longestDesc))
        val commandString = commandData.sortedBy { it.name }.joinToString("\n") { it.format(formatString) }

        return "$headerString\n$separator\n$commandString\n"
    }

    val keyString = buildString {
        with(commands) {
            if (any { it.arguments.any { it.isOptional } })
                appendLine("| (Argument)  | Argument is not required.      |")

            if (any { it.arguments.any { it is MultipleArg<*> } })
                appendLine("| Argument... | Accepts many of this argument. |")

            if (any { it.isFlexible })
                appendLine("| *Command    | Argument can be in any order.  |")
        }
    }

    val key =
        if (keyString.isNotBlank())
            """
                ## Key 
                | Symbol      | Meaning                        |
                | ----------- | ------------------------------ |
            """.trimIndent() + "\n$keyString\n"
        else
            ""

    val docs = commands
        .groupBy { it.category }
        .map { it.key to formatDocs(it.value.map { extractCommandData(it) }) }
        .sortedBy { it.first }
        .joinToString("") { "## ${it.first}\n${it.second}\n" }

    File("commands.md").writeText("# Commands\n\n$key$docs")
}
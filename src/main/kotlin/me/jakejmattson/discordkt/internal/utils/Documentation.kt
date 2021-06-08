package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.arguments.MultipleArg
import me.jakejmattson.discordkt.api.arguments.OptionalArg
import me.jakejmattson.discordkt.api.dsl.Command
import java.io.File
import kotlin.math.max

internal fun createDocumentation(commands: List<Command>) {
    if (commands.isEmpty())
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
        val nameString = command.names.joinToString().sanitizePipe()

        val expectedArgs = command.executions.map {
            it.arguments.joinToString {
                if (it is OptionalArg) "[${it.name}]" else it.name
            }.sanitizePipe().takeIf { it.isNotEmpty() } ?: ""
        }

        return CommandData(nameString, expectedArgs, command.description.sanitizePipe())
    }

    fun formatDocs(commandData: List<CommandData>): String {
        val header = CommandData("Commands", listOf("Arguments"), "Description")
        val longestName = max(commandData.maxLength { it.name }, header.name.length)
        val longestArgs = max(commandData.maxLength { it.longestArg }, header.args.first().length)
        val longestDesc = max(commandData.maxLength { it.desc }, header.desc.length)
        val formatString = "| %-${longestName}s | %-${longestArgs}s | %-${longestDesc}s |"

        val headerString = header.format(formatString)
        val separator = formatString.format("-".repeat(longestName), "-".repeat(longestArgs), "-".repeat(longestDesc))
        val commandString = commandData.sortedBy { it.name }.joinToString("\n") { it.format(formatString) }

        return "$headerString\n$separator\n$commandString\n"
    }

    val keyString = buildString {
        with(commands) {
            if (any { it.executions.any { it.arguments.any { it is OptionalArg } } })
                appendLine("| [Argument]  | Argument is not required.      |")

            if (any { it.executions.any { it.arguments.any { it is MultipleArg<*> } } })
                appendLine("| Argument... | Accepts many of this argument. |")
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
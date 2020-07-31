package me.jakejmattson.discordkt.internal.services

import me.jakejmattson.discordkt.api.dsl.command.*
import java.io.File

internal fun createDocumentation(container: CommandsContainer) {
    data class CategoryDocs(val name: String, val docString: String)

    data class CommandData(val name: String, val args: String, val description: String) {
        fun format(format: String) = String.format(format, name, args, description)
    }

    fun extractCommandData(command: Command): CommandData {
        val expectedArgs = command.arguments.joinToString {
            if (it.isOptional) "(${it.name})" else it.name
        }.takeIf { it.isNotEmpty() } ?: ""

        return CommandData(command.names.joinToString().replace("|", "\\|"),
            expectedArgs.replace("|", "\\|"),
            command.description.replace("|", "\\|"))
    }

    fun formatDocs(commandData: MutableList<CommandData>): String {
        val headerData = CommandData("Commands", "Arguments", "Description")

        commandData.add(headerData)
        val longestName = commandData.map { it.name.length }.max() ?: 0
        val longestArgs = commandData.map { it.args.length }.max() ?: 0
        val longestDescription = commandData.map { it.description.length }.max() ?: 0
        commandData.remove(headerData)

        val formatString = "| %-${longestName}s | %-${longestArgs}s | %-${longestDescription}s |"
        val headerString = headerData.format(formatString)
        val separator = formatString.format("-".repeat(longestName), "-".repeat(longestArgs), "-".repeat(longestDescription))
        val commandString = commandData.sortedBy { it.name }.joinToString("\n") { it.format(formatString) }

        return "$headerString\n$separator\n$commandString\n"
    }

    fun generateCategoryDoc(name: String, commands: List<Command>): CategoryDocs {
        val commandData = commands.map { extractCommandData(it) }
        val docs = formatDocs(commandData.toMutableList())

        return CategoryDocs(name, docs)
    }

    fun outputDocs(rawDocs: List<CategoryDocs>) {
        val docString = "# Commands\n\n" +
            "## Key\n" +
            "| Symbol     | Meaning                    |\n" +
            "| ---------- | -------------------------- |\n" +
            "| (Argument) | This argument is optional. |\n\n" +
            rawDocs.joinToString("") { "## ${it.name}\n${it.docString}\n" }

        File("commands.md").writeText(docString)
    }

    val docs = container.commands
        .groupBy { it.category }
        .map { generateCategoryDoc(it.key, it.value) }
        .sortedBy { it.name }

    outputDocs(docs)
}
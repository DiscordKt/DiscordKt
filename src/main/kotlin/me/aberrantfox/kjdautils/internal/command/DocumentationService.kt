package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.*
import java.io.File

private data class CategoryDocs(val name: String, val docString: String)

@Service
class DocumentationService(private val container: CommandsContainer) {
    fun generateDocumentation(outputType: DocumentationOutput, sortOrder: List<String>) {
        if (outputType == DocumentationOutput.NONE)
            return

        val categories = container.commands.values.groupBy { it.category }
        val categoryDocs = generateDocsByCategory(categories)
        val sortedDocs = sortCategoryDocs(categoryDocs, sortOrder)

        outputDocs(sortedDocs, outputType)
    }

    private fun generateDocsByCategory(categories: Map<String, List<Command>>) =
        categories.map { entry ->
            data class CommandData(val name: String, val args: String, val description: String) {
                fun format(format: String) = String.format(format, name, args, description)
            }

            //Map the commands to a data class for easier manipulation
            val commandData = entry.value.map { command ->
                CommandData(
                    command.name,
                    command.expectedArgs.joinToString {
                        if (it.optional)
                            "(${it.type.name})"
                        else
                            it.type.name
                    }.takeIf { it.isNotEmpty() } ?: "<none>",
                    command.description.replace("|", "\\|")
                )
            } as ArrayList

            with(commandData) {
                operator fun String.times(x: Int) = this.repeat(x)

                //Determine the max width of the data in each column (including headers)
                val headers = CommandData("Commands", "Arguments", "Description")
                add(headers)
                val longestName = maxBy { it.name.length }!!.name.length
                val longestArgs = maxBy { it.args.length }!!.args.length
                val longestDescription = maxBy { it.description.length }!!.description.length
                val columnFormat = "| %-${longestName}s | %-${longestArgs}s | %-${longestDescription}s |"
                remove(headers)

                //Apply the column format to the command data
                val docs = StringBuilder()
                docs.appendln(headers.format(columnFormat))
                docs.appendln(String.format(columnFormat, "-" * longestName, "-" * longestArgs, "-" * longestDescription))

                sortedBy { it.name }.forEach {
                    docs.appendln(it.format(columnFormat))
                }

                CategoryDocs(entry.key, docs.toString())
            }
        } as ArrayList<CategoryDocs>

    private fun sortCategoryDocs(categoryDocs: ArrayList<CategoryDocs>, sortOrder: List<String>): List<CategoryDocs> {
        val sortedMap = LinkedHashMap<String, CategoryDocs?>()
        val rogueCategories = arrayListOf<String>()

        //Populate the map keys with the desired sort order
        sortOrder.forEach {
            sortedMap[it] = null
        }

        //Populate the (sorted) map values with docs by name
        //If the sort order was not specified for a doc, it is appended to the end.
        categoryDocs.forEach {
            if (!sortedMap.containsKey(it.name))
                rogueCategories.add(it.name)

            sortedMap[it.name] = it
        }

        val deadCategories = sortedMap.filter { it.value == null }.map { it.key }

        with (rogueCategories) {
            if (isEmpty())
                return@with

            println("Found $size rogue categories not requested for sort. Appending to sorted docs: ${joinToString()}")
        }

        with (deadCategories) {
            if (isEmpty())
                return@with

            println("Found $size categories with no commands requested for sorting. Ignoring: ${joinToString()}")
        }

        //Remove dead keys (values with no data)
        return sortedMap.values.filterNotNull()
    }

    private fun outputDocs(rawDocs: List<CategoryDocs>, outputType: DocumentationOutput) {
        val indentLevel = "##"
        val header =
            "# Commands\n\n" +
                "$indentLevel Key\n" +
                "| Symbol     | Meaning                    |\n" +
                "| ---------- | -------------------------- |\n" +
                "| (Argument) | This argument is optional. |\n"

        val docsAsString = buildString {
            rawDocs.forEach {
                appendln("$indentLevel ${it.name}\n${it.docString}")
            }
        }

        val completeDocs = "$header\n$docsAsString"

        when (outputType) {
            DocumentationOutput.CONSOLE -> println(completeDocs)
            DocumentationOutput.FILE -> File("commands.md").writeText(completeDocs)
            DocumentationOutput.NONE -> Unit
        }
    }
}
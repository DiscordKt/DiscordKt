package me.aberrantfox.kjdautils.internal.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.internal.businessobjects.CategoryDocs
import me.aberrantfox.kjdautils.internal.businessobjects.CommandData
import me.aberrantfox.kjdautils.internal.businessobjects.CommandsOutputFormatter
import me.aberrantfox.kjdautils.internal.businessobjects.HEADER_DATA
import java.io.File

const val saveLocation = "commands.md"
val saveFile = File(saveLocation)

@Service
class DocumentationService(private val container: CommandsContainer) {
    fun obtainDocumentationString() = saveFile.readText()

    fun obtainDocumentationFile() = saveFile

    fun generateDocumentation(sortOrder: List<String>) {

        val categories = container.commands.values.groupBy { it.category }
        val categoryDocs = generateDocsByCategory(categories)

        val sortedDocs =
            if (sortOrder.isNotEmpty()) {
                sortCategoryDocs(categoryDocs, sortOrder)
            } else {
                categoryDocs.sortedBy { it.name }
            }

        outputDocs(sortedDocs)
    }

    private fun generateDocsByCategory(categories: Map<String, List<Command>>) =
            categories.map { generateSingleCategoryDoc(it) } as ArrayList<CategoryDocs>

    private fun sortCategoryDocs(categoryDocs: ArrayList<CategoryDocs>, categoryNameOrder: List<String>): List<CategoryDocs> {
        val sortedCategories = categoryDocs
                .filter { cat -> categoryNameOrder.any { it.toLowerCase() == cat.name.toLowerCase() } }
                .sortedBy { cat -> categoryNameOrder.indexOfFirst { it == cat.name } }.toMutableList()

        //add back anything that was missing
        sortedCategories.addAll(categoryDocs.filter { !sortedCategories.contains(it) })

        return sortedCategories.toList()
    }

    private fun outputDocs(rawDocs: List<CategoryDocs>) {
        val indentLevel = "##"
        val docsAsString =
            "# Commands\n\n" +
            "$indentLevel Key\n" +
            "| Symbol     | Meaning                    |\n" +
            "| ---------- | -------------------------- |\n" +
            "| (Argument) | This argument is optional. |\n\n" +
            buildString {
                rawDocs.forEach {
                    appendln("$indentLevel ${it.name}\n${it.docString}")
                }
            }

        saveFile.writeText(docsAsString)
    }

    private fun generateSingleCategoryDoc(entry: Map.Entry<String, List<Command>>): CategoryDocs {
        val commandData = entry.value.map { it.toCommandData() }
        val commandDataFormat = generateFormat(commandData)
        val separator = generateSeparator(commandDataFormat)

        val commandString = commandData
                .sortedBy { it.name }
                .joinToString("\n"){ it.format(commandDataFormat.generateFormatString()) }

        val docs =
            """;;-${HEADER_DATA.format(commandDataFormat.generateFormatString())}
               ;;-$separator
               ;;-$commandString
               ;;-
            """.trimMargin(";;-")

        return CategoryDocs(entry.key, docs)
    }

    private fun generateSeparator(cformat: CommandsOutputFormatter) = with(cformat) {
        String.format(cformat.generateFormatString(), "-".repeat(longestName), "-".repeat(longestArgs), "-".repeat(longestDescription))
    }

    private fun generateFormat(commandData: List<CommandData>): CommandsOutputFormatter {
        val longestName = commandData.maxBy { it.name.length }!!.name.length
        val longestArgs = commandData.maxBy { it.args.length }!!.args.length
        val longestDescription = commandData.maxBy { it.description.length }!!.description.length

        return CommandsOutputFormatter().apply {
            //check to see if any of the real data was longer than our pre-defined default values
            this.longestArgs = maxOf(this.longestArgs, longestArgs)
            this.longestName = maxOf(this.longestName, longestName)
            this.longestDescription = maxOf(this.longestDescription, longestDescription)
        }
    }

}
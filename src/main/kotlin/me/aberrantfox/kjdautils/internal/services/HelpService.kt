package me.aberrantfox.kjdautils.internal.services

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.stdlib.randomListItem
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.command.CommandRecommender
import java.awt.Color

class HelpService(private val container: CommandsContainer, private val config: KConfiguration) {
    fun produceHelpCommandContainer() = commands {
        command("Help") {
            description = "Display a help menu."
            category = "Utility"
            expect(arg(WordArg("Category or Command"), true, ""))
            execute {
                val query = it.args.component1() as String

                val responseEmbed = when {
                    query.isEmpty() -> generateDefaultEmbed(it)
                    query.isCommand(it) -> generateCommandEmbed(container[query]!!)
                    query.isCategory(it) -> generateCategoriesEmbed(query, it)
                    else -> generateRecommendationEmbed(query, it)
                }

                it.respond(responseEmbed)
            }
        }
    }

    private fun generateDefaultEmbed(event: CommandEvent) =
        embed {
            title = "Help menu"
            description = "Use ${config.prefix}help <command|category> for more information"
            color = Color.decode("#00E58D")

            val categoryMap = container.commands.values
                .filter { it.isVisible(event) }
                .groupBy { it.category }

            categoryMap.toList()
                .sortedBy { (_, commands) -> -commands.size }
                .map { (category, commands) ->
                    field {
                        name = category
                        value = commands.sortedBy { it.name }.joinToString("\n") { it.name }
                        inline = true
                    }
            }
        }

    private fun generateCommandEmbed(command: Command) = embed {
        title = "Displaying help for ${command.name}"
        description = command.description
        color = Color.CYAN

        val commandInvocation = "${config.prefix}${command.name} "
        addField("What is the structure of the command?", "$commandInvocation ${generateStructure(command)}")
        addField("Show me an example of someone using the command.", "$commandInvocation ${generateExample(command)}")
    }

    private fun generateCategoriesEmbed(category: String, event: CommandEvent) =
        embed {
            val commands = container.commands.values
                .filter { it.category.toLowerCase() == category.toLowerCase() }
                .filter { it.isVisible(event) }
                .map { it.name }
                .reduceRight{a, b -> "$a, $b"}

            title = "Displaying commands in the $category category"
            description = commands.toLowerCase()
            color = Color.decode("#00C4A6")
        }

    private fun generateRecommendationEmbed(query: String, event: CommandEvent) =
        embed {
            val recommendation = CommandRecommender.recommendCommand(query) { it.isVisible(event) }

            title = "Could not find a category or command with that name."
            description = "Did you mean $recommendation?\nMaybe you should try ${config.prefix}help"
            color = Color.RED
        }

    private fun generateStructure(command: Command) =
        command.expectedArgs.joinToString(" ") {
            val type = it.type.name
            if (it.optional) "($type)" else "[$type]"
        }

    private fun generateExample(command: Command) =
        command.expectedArgs.joinToString(" ") {
            it.type.examples.randomListItem()
        }

    private fun String.isCategory(event: CommandEvent) = container.commands.values
        .any {
            this.toLowerCase() == it.category.toLowerCase() && it.isVisible(event)
        }

    private fun String.isCommand(event: CommandEvent) = container.commands.values
        .any {
            this.toLowerCase() == it.name.toLowerCase() && it.isVisible(event)
        }

    private fun Command.isVisible(event: CommandEvent) =
        config.visibilityPredicate(this, event.author, event.channel, event.guild)
}
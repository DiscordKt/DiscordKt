package me.aberrantfox.kjdautils.internal.services

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.extensions.stdlib.randomListItem
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.command.CommandRecommender
import java.awt.Color

class HelpService(private val container: CommandsContainer, private val config: KConfiguration) {
    fun produceHelpCommandContainer() = commands {
        command("Help") {
            description = "Display a help menu."
            category = "Utility"
            execute(WordArg("Command").makeOptional("")) {
                val query = it.args.component1()

                val responseEmbed = when {
                    query.isEmpty() -> generateDefaultEmbed(it)
                    query.isCommand(it) -> generateCommandEmbed(container[query]!!, query)
                    else -> generateRecommendationEmbed(query, it)
                }

                it.respond(responseEmbed)
            }
        }
    }

    private fun generateDefaultEmbed(event: CommandEvent<*>) =
        embed {
            title = "Help menu"
            description = "Use `${config.prefix}help <command>` for more information."
            color = Color.decode("#00E58D")

            val categoryMap = fetchVisibleCommands(event).groupBy { it.category }

            categoryMap.toList()
                .sortedBy { (_, commands) -> -commands.size }
                .map { (category, commands) ->
                    field {
                        name = category
                        value = commands.sortedBy { it.names.joinToString() }.joinToString("\n") { it.names.joinToString() }
                        inline = true
                    }
            }
        }

    private fun generateCommandEmbed(command: Command, input: String) = embed {
        title = command.names.joinToString()
        description = command.description
        color = Color.CYAN

        val commandInvocation = "${config.prefix}$input"
        addField("What is the structure of the command?", "$commandInvocation ${generateStructure(command)}")
        addField("Show me an example of someone using the command.", "$commandInvocation ${generateExample(command)}")
    }

    private fun generateRecommendationEmbed(query: String, event: CommandEvent<*>) =
        embed {
            val recommendation = CommandRecommender.recommendCommand(query) { it.isVisible(event) }

            title = "Could not find a command with that name."
            description = "Did you mean $recommendation?\nMaybe you should try ${config.prefix}help"
            color = Color.RED
        }

    private fun generateStructure(command: Command) =
        command.expectedArgs.arguments.joinToString(" ") {
            val type = it.name
            if (it.isOptional) "($type)" else "[$type]"
        }

    private fun generateExample(command: Command) =
        command.expectedArgs.arguments.joinToString(" ") {
            it.examples.randomListItem()
        }

    private fun String.isCommand(event: CommandEvent<*>) = fetchVisibleCommands(event)
        .any {
            this.toLowerCase() in it.names.map { it.toLowerCase() }
        }

    private fun fetchVisibleCommands(event: CommandEvent<*>) = container.commands.filter { it.isVisible(event) }

    private fun Command.isVisible(event: CommandEvent<*>) =
        config.visibilityPredicate(this, event.author, event.channel, event.guild)
}
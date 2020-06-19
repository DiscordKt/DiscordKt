package me.jakejmattson.kutils.internal.services

import me.jakejmattson.kutils.api.arguments.AnyArg
import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.dsl.configuration.BotConfiguration
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.jakejmattson.kutils.internal.command.CommandRecommender

internal class HelpService(private val container: CommandsContainer, private val config: BotConfiguration) {
    fun produceHelpCommandContainer() = commands {
        command("Help") {
            description = "Display a help menu."
            category = "Utility"
            execute(AnyArg("Command").makeOptional("")) {
                val query = it.args.component1()

                val responseEmbed = when {
                    query.isEmpty() -> generateDefaultEmbed(it)
                    query.isCommand(it) -> generateCommandEmbed(container[query]!!, it, query)
                    else -> generateRecommendationEmbed(query, it)
                }

                it.respond(responseEmbed)
            }
        }
    }

    private fun generateDefaultEmbed(event: CommandEvent<*>) =
        embed {
            simpleTitle = "Help menu"
            description = "Use `${event.relevantPrefix}help <command>` for more information."
            color = infoColor

            fetchVisibleCommands(event)
                .groupBy { it.category }
                .toList()
                .sortedBy { (_, commands) -> -commands.size }
                .map { (category, commands) ->
                    field {
                        name = category
                        value = "```css\n" +
                            commands
                                .sortedBy { it.names.joinToString() }
                                .joinToString("\n")
                                { it.names.joinToString() } +
                            "\n```"
                        inline = true
                    }
                }
        }

    private fun generateCommandEmbed(command: Command, event: CommandEvent<*>, input: String) = embed {
        simpleTitle = command.names.joinToString()
        description = command.description
        color = infoColor

        val commandInvocation = "${event.relevantPrefix}$input"
        addField("Structure", "$commandInvocation ${generateStructure(command)}")

        if (command.parameterCount != 0)
            addField("Examples", "$commandInvocation ${generateExample(command, event)}")
    }

    private fun generateRecommendationEmbed(query: String, event: CommandEvent<*>) =
        CommandRecommender.buildRecommendationEmbed(query) { it.isVisible(event) }

    private fun generateExample(command: Command, event: CommandEvent<*>) =
        command.arguments.joinToString(" ") {
            val examples = it.generateExamples(event)
            val example = if (examples.isNotEmpty()) examples.random() else "<Example>"

            if (it.isOptional) "($example)" else "[$example]"
        }

    private fun String.isCommand(event: CommandEvent<*>) = fetchVisibleCommands(event)
        .any {
            this.toLowerCase() in it.names.map { it.toLowerCase() }
        }

    private fun fetchVisibleCommands(event: CommandEvent<*>) = container.commands.filter { it.isVisible(event) }

    private fun Command.isVisible(event: CommandEvent<*>) =
        config.visibilityPredicate(this, event.author, event.channel, event.guild)
}

internal fun generateStructure(command: Command) =
    command.arguments.joinToString(" ") {
        val type = it.name
        if (it.isOptional) "($type)" else "[$type]"
    }
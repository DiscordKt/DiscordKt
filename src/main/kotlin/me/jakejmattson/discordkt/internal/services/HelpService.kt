package me.jakejmattson.discordkt.internal.services

import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.dsl.*
import me.jakejmattson.discordkt.internal.utils.Recommender
import java.awt.Color

internal fun produceHelpCommand() = commands("Utility") {
    command("Help") {
        description = "Display a help menu."
        execute(AnyArg("Command").makeOptional("")) {
            val query = args.first
            val color = discord.configuration.theme

            when {
                query.isEmpty() -> sendDefaultEmbed(this, color)
                query.isCommand(this) -> sendCommandEmbed(discord.commands[query]!!, this, query, color)
                else -> Recommender.sendRecommendation(this, query, fetchVisibleCommands(this).flatMap { it.names })
            }
        }
    }
}

private suspend fun sendDefaultEmbed(event: GlobalCommandEvent<*>, embedColor: Color?) =
    event.respond {
        title = "Help menu"
        description = "Use `${event.prefix()}help <command>` for more information."
        color = embedColor

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

private suspend fun sendCommandEmbed(command: Command, event: GlobalCommandEvent<*>, input: String, embedColor: Color?) =
    event.respond {
        title = command.names.joinToString()
        description = command.description
        color = embedColor

        val commandInvocation = "${event.prefix()}$input"

        field {
            name = "Structure"
            value = "$commandInvocation ${generateStructure(command)}"
        }

        if (command.parameterCount != 0)
            field {
                name = "Examples"
                value = "$commandInvocation ${generateExample(command, event)}"
            }
    }

private fun generateExample(command: Command, event: GlobalCommandEvent<*>) =
    command.arguments.joinToString(" ") {
        val examples = it.generateExamples(event)
        val example = if (examples.isNotEmpty()) examples.random() else "<Example>"

        if (it.isOptional) "($example)" else "[$example]"
    }

private suspend fun String.isCommand(event: GlobalCommandEvent<*>) = fetchVisibleCommands(event)
    .any { toLowerCase() in it.names.map { it.toLowerCase() } }

private suspend fun fetchVisibleCommands(event: GlobalCommandEvent<*>) = event.discord.commands
    .filter { event.discord.configuration.permissions.invoke(it, event.discord, event.author, event.channel, event.guild) }

private fun generateStructure(command: Command) =
    command.arguments.joinToString(" ") {
        val type = it.name
        if (it.isOptional) "($type)" else "[$type]"
    }


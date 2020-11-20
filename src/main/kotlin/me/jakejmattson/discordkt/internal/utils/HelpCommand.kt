package me.jakejmattson.discordkt.internal.utils

import com.gitlab.kordlib.common.kColor
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.dsl.*
import java.awt.Color

internal fun produceHelpCommand() = commands("Utility") {
    command("Help") {
        description = "Display a help menu."
        execute(AnyArg("Command").makeOptional("")) {
            val input = args.first
            val theme = discord.configuration.theme

            if (input.isEmpty())
                sendDefaultEmbed(theme)
            else
                discord.commands[input]?.sendHelpEmbed(this, input, theme)
                    ?: Recommender.sendRecommendation(this, input)
        }
    }
}

private suspend fun CommandEvent<*>.sendDefaultEmbed(embedColor: Color?) =
    respond {
        title = "Help menu"
        description = "Use `${prefix()}help <command>` for more information."
        color = embedColor?.kColor

        discord.commands
            .filter { discord.configuration.hasPermission(it, this@sendDefaultEmbed) }
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

private suspend fun Command.sendHelpEmbed(event: CommandEvent<*>, input: String, embedColor: Color?) =
    event.respond {
        title = names.joinToString()
        description = this@sendHelpEmbed.description
        color = embedColor?.kColor

        val commandInvocation = "${event.prefix()}$input"

        field {
            name = "Structure"
            value = "$commandInvocation ${generateStructure()}"
        }

        if (parameterCount != 0)
            field {
                name = "Examples"
                value = "$commandInvocation ${generateExample(event)}"
            }
    }

private fun Command.generateExample(event: CommandEvent<*>) =
    arguments.joinToString(" ") {
        val examples = runBlocking { it.generateExamples(event) }
        val example = if (examples.isNotEmpty()) examples.random() else "<Example>"

        if (it.isOptional) "($example)" else "[$example]"
    }

private fun Command.generateStructure() =
    arguments.joinToString(" ") {
        val type = it.name
        if (it.isOptional) "($type)" else "[$type]"
    }
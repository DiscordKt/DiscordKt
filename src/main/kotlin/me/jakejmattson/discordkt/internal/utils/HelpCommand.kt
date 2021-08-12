package me.jakejmattson.discordkt.internal.utils

import dev.kord.common.kColor
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.arguments.Argument
import me.jakejmattson.discordkt.api.dsl.*
import java.awt.Color

internal fun produceHelpCommand(category: String) = commands(category) {
    command(discord.locale.helpName) {
        description = discord.locale.helpDescription
        execute(AnyArg("Command").optional("")) {
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
        title = discord.locale.helpName
        description = discord.locale.helpEmbedDescription
        color = embedColor?.kColor

        discord.commands
            .filter { it.hasPermissionToRun(this@sendDefaultEmbed) }
            .groupBy { it.category }
            .toList()
            .sortedBy { (_, commands) -> -commands.size }
            .map { (category, commands) ->
                field {
                    name = category
                    value = "```\n" +
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
        color = embedColor?.kColor

        if (this@sendHelpEmbed.description.isNotBlank())
            description = this@sendHelpEmbed.description

        val commandInvocation = "${event.prefix()}$input"

        val helpBundle = this@sendHelpEmbed.executions.map {
            """$commandInvocation ${it.structure}
                ${
                it.arguments.joinToString("\n") { arg ->
                    """- ${arg.name}: ${arg.description} (${arg.generateExample(event)})
                    """.trimMargin()
                }
            }
            """.trimMargin()
        }

        field {
            this.value = helpBundle.joinToString("\n\n") { it }
        }
    }

private fun Argument<*>.generateExample(event: CommandEvent<*>) =
    runBlocking { generateExamples(event) }
        .takeIf { it.isNotEmpty() }
        ?.random()
        ?: "<Example>"
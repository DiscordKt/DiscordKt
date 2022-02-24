package me.jakejmattson.discordkt.internal.utils

import dev.kord.common.Color
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.commands.*

internal fun produceHelpCommand(category: String) = commands(category) {
    globalCommand(discord.locale.helpName) {
        description = discord.locale.helpDescription
        requiredPermission = discord.permissions.commandDefault
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
        color = embedColor

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
        color = embedColor

        if (this@sendHelpEmbed.description.isNotBlank())
            description = this@sendHelpEmbed.description

        val commandInvocation = "${event.prefix()}$input"

        val helpBundle = this@sendHelpEmbed.executions.map {
            """$commandInvocation ${it.structure}
                ${
                it.arguments.joinToString("\n") { arg ->
                    """- ${arg.name}: ${arg.description} (${arg.generateExample(event.context)})
                    """.trimMargin()
                }
            }
            """.trimMargin()
        }

        field {
            this.value = helpBundle.joinToString("\n\n") { it }
        }
    }

private fun Argument<*, *>.generateExample(context: DiscordContext) =
    runBlocking { generateExamples(context) }
        .takeIf { it.isNotEmpty() }
        ?.random()
        ?: "<Example>"
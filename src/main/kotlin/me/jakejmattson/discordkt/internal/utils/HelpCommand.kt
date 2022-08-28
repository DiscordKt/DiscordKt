package me.jakejmattson.discordkt.internal.utils

import dev.kord.common.Color
import dev.kord.core.entity.interaction.GuildAutoCompleteInteraction
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.commands.*

internal fun produceHelpCommand(category: String) = commands(category) {
    slash(discord.locale.helpName, discord.locale.helpDescription, discord.configuration.defaultPermissions) {
        execute(AnyArg("Command")
            .autocomplete {
                discord.commands
                    .filter { it.hasPermissionToRun(discord, interaction.user, (interaction as GuildAutoCompleteInteraction).getGuild()) }
                    .map { it.names }.flatten()
                    .filter { it.contains(input, true) }
            }.optional("")) {
            val input = args.first
            val theme = discord.configuration.theme

            if (input.isEmpty())
                sendDefaultEmbed(theme)
            else
                discord.commands.findByName(input)?.sendHelpEmbed(this, input, theme)
                    ?: Recommender.sendRecommendation(this, input)
        }
    }
}

private suspend fun CommandEvent<*>.sendDefaultEmbed(embedColor: Color?) =
    respond {
        title = discord.locale.helpName
        description = discord.locale.helpEmbedDescription
        color = embedColor

        val commandGroups =
            discord.commands
                .filter { it.hasPermissionToRun(discord, this@sendDefaultEmbed.author, guild) }
                .groupBy { it.category }
                .toList()

        val subcommandGroups = discord.subcommands
            .filter { it.commands.first().hasPermissionToRun(discord, this@sendDefaultEmbed.author, guild) }
            .map { "/${it.name}" to it.commands }

        (commandGroups + subcommandGroups)
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
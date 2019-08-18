package me.aberrantfox.kjdautils.internal.services

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.extensions.stdlib.randomListItem
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.command.CommandRecommender
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

enum class SelectionArgument { CommandName, CategoryName }

class HelpService(private val container: CommandsContainer, private val config: KConfiguration) {
    fun produceHelpCommandContainer() = commands {
        command("Help") {
            description = "Display a help menu."
            category = "Utility"
            expect(arg(WordArg("Category or Command"), true, null))
            execute {
                val query = it.args.component1() as String?
                    ?: return@execute it.respond(defaultEmbed(it))

                when(fetchArgumentType(query, it)) {
                    SelectionArgument.CommandName -> {
                        val command = container[query]!!

                        it.respond(generateCommandEmbed(command))
                    }

                    SelectionArgument.CategoryName -> {
                        it.respond(generateCategoriesEmbed(query, it))
                    }

                    null -> it.respond(embed{
                        title = "The category or command $query does not exist"
                        val recommendation = CommandRecommender.recommendCommand(query)
                            { cmd -> config.visibilityPredicate(cmd, it.author, it.channel, it.guild) }
                        description = "Did you mean $recommendation ?\n" +
                            "Maybe you should try ${config.prefix}help"
                        color = Color.RED
                    })
                }
            }
        }
    }

    private fun generateCommandEmbed(command: Command) = embed {
        title = "Displaying help for ${command.name}"
        description = command.description
        color = Color.CYAN
        val commandInvocation = "${config.prefix}${command.name} "

        field {
            name = "What is the structure of the command?"
            value = "$commandInvocation ${generateStructure(command)}"
            inline = false
        }

        field {
            name = "Show me an example of someone using the command."
            value = "$commandInvocation ${generateExample(command)}"
            inline = false
        }
    }

    private fun generateCategoriesEmbed(category: String, event: CommandEvent) : MessageEmbed {
        val commands = container.commands
                .filter { it.component2().category.toLowerCase() == category.toLowerCase() }
                .map { it.component2().name }
                .filter { config.visibilityPredicate(it.toLowerCase(), event.author, event.channel, event.guild) }
                .reduceRight{a, b -> "$a, $b"}

        return embed {
            title = "Displaying commands in the $category category"
            description = commands.toLowerCase()
            color = Color.decode("#00C4A6")
        }
    }

    private fun defaultEmbed(event: CommandEvent) :MessageEmbed {
        val commands = container.commands.values.asSequence()
            .groupBy { it.category }
            .toList().distinct()
            .filter { it.second.isNotEmpty() }
            .filter { config.visibilityPredicate(it.first.toLowerCase(), event.author, event.channel, event.guild) }
            .sortedBy { (_, value) -> -value.size }
            .toList().toMap()

        return embed {
            title = "Help menu"
            description = "Use ${config.prefix}help <command|category> for more information"
            color = Color.decode("#00E58D")

            commands.forEach {
                addInlineField(it.key, it.value.sortedBy { it.name }.joinToString("\n") { it.name })
            }
        }
    }

    private fun generateStructure(command: Command) =
            command.expectedArgs.joinToString(" ") {
                if (it.optional) {
                    "(${it.type.name})"
                } else {
                    "[${it.type.name}]"
                }
            }

    private fun generateExample(command: Command) =
            command.expectedArgs.joinToString(" ") {
                it.type.examples.randomListItem()
            }

    private fun fetchArgumentType(value: String, event: CommandEvent): SelectionArgument?{
        val isCategory = container.commands.any {
            it.component2().category.toLowerCase() == value.toLowerCase()
                    && config.visibilityPredicate(it.key.toLowerCase(), event.author, event.channel, event.guild)
        }

        if(isCategory) return SelectionArgument.CategoryName

        val isCommand = container.commands.any {
            it.component2().name.toLowerCase() == value.toLowerCase()
                    && config.visibilityPredicate(value.toLowerCase(), event.author, event.channel, event.guild)
        }
        if(isCommand) return SelectionArgument.CommandName

        return null
    }
}
package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.stdlib.randomListItem
import me.aberrantfox.kjdautils.internal.command.arguments.WordArg
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color

enum class SelectionArgument { CommandName, CategoryName }

class HelpService(val container: CommandsContainer, val prefix: String) {

    init {
        container.command("help") {
            description = "Display a help menu"
            category = "utility"
            expect(arg(WordArg, true))

            execute {
                val query = it.args.component1() as String

                if(query.isEmpty()){ it.respond(defaultEmbed()); return@execute }

                when(fetchArgumentType(query)) {
                    SelectionArgument.CommandName -> {
                        val command = container[query.toLowerCase()]!!

                        it.respond(generateCommandEmbed(command))
                    }

                    SelectionArgument.CategoryName -> {
                        it.respond(generateCategoriesEmbed(query))
                    }

                    null -> it.respond(embed{
                        title("The category or command $query does not exist")
                        val recommendation = CommandRecommender.recommendCommand(query)
                        setDescription("Did you mean $recommendation ?\n" +
                                       "Maybe you should try ${prefix}help")
                        setColor(Color.RED)
                    })
                }
            }
        }
    }

    private fun generateCommandEmbed(command: Command) = embed {
        title("Displaying help for ${command.name}")
        description(command.description)
        setColor(Color.CYAN)
        val commandInvocation = "$prefix${command.name} "

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

    private fun generateCategoriesEmbed(category: String) :MessageEmbed {
        val commands = container.commands
                .filter { it.component2().category.toLowerCase() == category.toLowerCase() }
                .map { it.component2().name }
                .reduceRight{a, b -> "$a, $b"}

        return embed {
            setTitle("Displaying commands in the $category category")
            setDescription(commands.toLowerCase())
            setColor(Color.decode("#00C4A6"))
        }
    }

    private fun defaultEmbed() :MessageEmbed {
        val categories = container.commands
                .map { it.component2().category }
                .distinct()
                .reduceRight {a, b -> "$a, $b" }

        return embed {
            setTitle("Help menu")
            setDescription("Use ${prefix}help <command|category> for more information")
            setColor(Color.decode("#00E58D"))

            field {
                name = "Currently available categories"
                value = categories
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

    private fun fetchArgumentType(value: String): SelectionArgument?{
        val isCategory = container.commands.any { it.component2().category.toLowerCase() == value.toLowerCase() }
        if(isCategory) return SelectionArgument.CategoryName

        val isCommand = container.commands.any { it.component2().name.toLowerCase() == value.toLowerCase() }
        if(isCommand) return SelectionArgument.CommandName

        return null
    }
}
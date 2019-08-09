package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.stdlib.randomListItem
import me.aberrantfox.kjdautils.internal.command.arguments.WordArg
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

enum class SelectionArgument { CommandName, CategoryName }

class HelpService(private val container: CommandsContainer, private val config: KConfiguration) {
    init {
        container.command("help") {
            description = "Display a help menu"
            category = "utility"
            expect(arg(WordArg, true))

            execute {
                val query = it.args.component1() as String

                if(query.isEmpty()){ it.respond(defaultEmbed(it)); return@execute }

                when(fetchArgumentType(query, it)) {
                    SelectionArgument.CommandName -> {
                        val command = container[query.toLowerCase()]!!

                        it.respond(generateCommandEmbed(command))
                    }

                    SelectionArgument.CategoryName -> {
                        it.respond(generateCategoriesEmbed(query, it))
                    }

                    null -> it.respond(embed{
                        title = "The category or command $query does not exist"
                        val recommendation = CommandRecommender.recommendCommand(query,
                                { cmd -> config.visibilityPredicate(cmd, it.author, it.channel, it.guild) })
                        setDescription("Did you mean $recommendation ?\n" +
                                       "Maybe you should try ${config.prefix}help")
                        setColor(Color.RED)
                    })
                }
            }
        }
        CommandRecommender.addPossibility("help")
    }

    private fun generateCommandEmbed(command: Command) = embed {
        title = "Displaying help for ${command.name}"
        description = command.description
        setColor(Color.CYAN)
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
            setTitle("Displaying commands in the $category category")
            setDescription(commands.toLowerCase())
            setColor(Color.decode("#00C4A6"))
        }
    }

    private fun defaultEmbed(event: CommandEvent) :MessageEmbed {
        val categories = container.commands
                .filter { config.visibilityPredicate(it.key.toLowerCase(), event.author, event.channel, event.guild) }
                .map { it.component2().category }
                .distinct()
                .filter { it.isNotBlank() }
                .reduceRight { a, b -> "$a, $b" }

        return embed {
            setTitle("Help menu")
            setDescription("Use ${config.prefix}help <command|category> for more information")
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
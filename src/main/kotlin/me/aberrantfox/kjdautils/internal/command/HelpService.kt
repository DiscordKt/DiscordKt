package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.stdlib.randomListItem
import me.aberrantfox.kjdautils.internal.command.arguments.CommandArg
import java.awt.Color

class HelpService(val container: CommandsContainer, val prefix: String) {

    init {
        val helpMenu = container.command("menu") {
            description = "If you forget how something works, just use this command."
            category = "utility"
            execute {
                it.respond(embed{
                    title("Help menu")
                    field {
                        name = "Wondering how a command works?"
                        value = "${prefix}help <command name>, e.g. ${prefix}help help"
                    }
                })
            }
        }

        container.command("help") {
            category = "utility"
            expect(arg(CommandArg, true, helpMenu!!))
            execute {
                val command = it.args.component1() as Command
                it.respond(generateEmbed(command))
            }
        }
    }

    private fun generateEmbed(command: Command) = embed {
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
}
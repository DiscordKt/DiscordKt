package me.aberrantfox.kjdautils.examples.implementations

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

@CommandSet("DSL Demo")
fun dslCommands() = commands {
    //This command demonstrates the custom DSL for creating a Discord embed
    command("Embed") {
        description = "Display an example embed."
        execute {
            val embed =
                embed {
                    title = "This is the title."
                    description = "This is the description."

                    author {
                        name = it.author.fullName()
                        iconUrl = it.author.effectiveAvatarUrl
                    }

                    field {
                        name = "This is a field."
                        value = "Fields can have titles and descriptions."
                    }

                    footer {
                        iconUrl = it.discord.jda.selfUser.effectiveAvatarUrl
                        text = "This is some footer text."
                    }
                }

            it.respond(embed)
        }
    }

    //This command demonstrates the custom DSL for creating a menu
    //A menu is an embed with pagination support and interactive reaction buttons
    command("Menu") {
        description = "Display an example menu."
        execute {
            it.respond(
                menu {
                    embed {
                        title = "Page 1"
                    }

                    embed {
                        title = "Page 2"
                    }

                    reaction("\uD83C\uDF08") { currentEmbed: EmbedBuilder ->
                        val randomColor = Color((0..255).random(), (0..255).random(), (0..255).random())
                        currentEmbed.setColor(randomColor)
                    }
                }
            )
        }
    }
}
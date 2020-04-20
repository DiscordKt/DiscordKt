package me.aberrantfox.kjdautils.examples

import com.google.gson.Gson
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

data class Properties(val version: String, val repository: String)

private val propFile = Properties::class.java.getResource("/properties.json").readText()
private val project = Gson().fromJson(propFile, Properties::class.java)

fun main(args: Array<String>) {
    val token = args.firstOrNull()
        ?: throw IllegalArgumentException("No program arguments provided. Expected bot token.")

    startBot(token) {
        configure {
            prefix = "!"
            allowMentionPrefix = true

            colors {
                successColor = Color.GREEN
                failureColor = Color.RED
                infoColor = Color.BLUE
            }

            mentionEmbed { event ->
                val self = event.guild.jda.selfUser

                color = Color(0x00bfff)
                thumbnail = self.effectiveAvatarUrl
                addField(self.fullName(), "This is an example embed that can be created whenever the bot is pinged.")
                addInlineField("Prefix", prefix)

                with(project) {
                    addField("Build Info", "```" +
                        "Version: $version\n" +
                        "Kotlin:  ${KotlinVersion.CURRENT}" +
                        "```")

                    addInlineField("Source", repository)
                }
            }
        }
    }
}

@CommandSet("Utility")
fun utilityCommands() = commands {
    //Command with no args and multiple names
    command("Version", "V") {
        description = "Display the version."
        execute {
            it.respond(project.version)
        }
    }
}
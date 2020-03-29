package me.aberrantfox.kjdautils.examples


import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.api.startBot

data class MyCustomBotConfiguration(val version: String)

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: throw IllegalArgumentException("No program arguments provided. Expected bot token.")

    startBot(token) {
        val myConfig = MyCustomBotConfiguration("0.1.0")

        registerInjectionObject(myConfig)

        configure {
            prefix = "!"
            mentionEmbed = { event ->
                embed {
                    val name = event.guild.name

                    title = "Hello ${event.author.asTag}!"
                    description = "I was mentioned in $name"
                }
            }
        }
    }
}

@CommandSet("Utility")
fun commandSet(myConfig: MyCustomBotConfiguration) = commands {
    //Command with no args and multiple names
    command("Version", "V") {
        description = "Display the version."
        execute {
            it.respond(myConfig.version)
        }
    }
}
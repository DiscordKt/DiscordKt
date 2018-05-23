package me.aberrantfox.kjdautils.examples


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.command.Sentence
import me.aberrantfox.kjdautils.internal.command.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.text.SimpleDateFormat

data class MyCustomBotConfiguration(val version: String , val token: String)

data class MyCustomLogger(val prefix: String) {
    fun log(data: String) = println(data)
}

fun main(args: Array<String>) {
    val token = args.component1()
    val prefix = "!"
    val commandPath =  "me.aberrantfox.kjdautils.examples"

    startBot(token) {
        val myConfig = MyCustomBotConfiguration("0.1.0", token)
        val myLog = MyCustomLogger(":: BOT ::")
        registerInjectionObject(myConfig, myLog)
        registerCommands(commandPath, prefix)
        registerListener(MessageLogger())
    }
}

class MessageLogger {
    @Subscribe fun onMessage(event: GuildMessageReceivedEvent) = println(event.message.contentRaw)
}

@CommandSet
fun defineOther(log: MyCustomLogger) = commands {
    command("someCommand") {
        execute { log.log("Hello, World!") }
    }
}

@CommandSet
fun helpCommand(myConfig: MyCustomBotConfiguration, log: MyCustomLogger) = commands {
    command("version") {
        execute {
            it.respond(myConfig.version)
            log.log("Version logged!")
        }
    }
    command("help") {
        execute {
            it.respond(embed {
                title("Help menu")
                description("Below you can see how to use all of the commands in this startBot")

                field {
                    name = "Help"
                    value = "Display a help menu"
                }

                field {
                    name = "Ping"
                    value = "Pong"
                }

                field {
                    name = "Echo"
                    value = "Echo the command arguments in the current channel."
                }
            })
        }
    }

    command("ping") {
        execute {
            it.respond("Pong!")
        }
    }

    command("echo") {
        expect(Sentence)
        execute {
            val response = it.args.component1() as String
            it.respond(response)
        }
    }

    command("joindate") {
        expect(User)
        execute {
            val target = it.args.component1() as net.dv8tion.jda.core.entities.User
            val member = it.author.mutualGuilds.first().getMember(target)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val joinDateParsed = dateFormat.parse(member.joinDate.toString())
            val joinDate = dateFormat.format(joinDateParsed)

            it.respond("${member.fullName()}'s join date: $joinDate")
        }
    }
}
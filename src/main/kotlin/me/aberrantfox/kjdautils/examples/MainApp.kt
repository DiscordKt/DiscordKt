package me.aberrantfox.kjdautils.examples


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.command.arguments.SentenceArg
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

data class MyCustomBotConfiguration(val version: String, val token: String)

data class MyCustomLogger(val prefix: String) {
    fun log(data: String) = println(data)
}

fun main(args: Array<String>) {
    val token = args.component1()
    val prefix = "!"
    val commandPath = "me.aberrantfox.kjdautils.examples"

    startBot(token) {
        val myConfig = MyCustomBotConfiguration("0.1.0", token)
        val myLog = MyCustomLogger(":: BOT ::")

        registerInjectionObject(myConfig, myLog)
        registerCommands(commandPath, prefix)
        registerListenersByPath("me.aberrantfox.kjdautils.examples")

        registerCommandPreconditions({
            if (it.channel.name != "ignored") {
                Pass
            } else {
                Fail()
            }
        }, {
            if (it.author.discriminator == "3693") {
                Fail("Ignoring users with your discriminator.")
            } else {
                Pass
            }
        })
    }
}

class MessageLogger(val myConfig: MyCustomBotConfiguration) {
    @Subscribe
    fun onMessage(event: GuildMessageReceivedEvent) {
        println("ExampleBot :: V${myConfig.version} :: ${event.message.contentRaw}")
    }
}

@CommandSet
fun defineOther(log: MyCustomLogger) = commands {
    command("someCommand") {
        execute { log.log("Hello, World!") }
    }
}

@CommandSet("utility")
fun commandSet(myConfig: MyCustomBotConfiguration, log: MyCustomLogger) = commands {
    command("version") {
        description = "A command which will show the verison."
        category = "info"
        execute {
            it.respond(myConfig.version)
            log.log("Version logged!")
        }
    }


    command("echo") {
        expect(SentenceArg)
        execute {
            val response = it.args.component1() as String
            it.respond(response)
        }
    }

    command("add") {
        description = "Add two numbers together"
        expect(IntegerArg, IntegerArg)
        execute {
            val first = it.args.component1() as Int
            val second = it.args.component2() as Int

            it.respond("${first + second}")
        }
    }

    command("optionalAdd") {
        description = "Add two numbers together"
        expect(arg(IntegerArg, false), arg(IntegerArg, true, 1))
        execute {
            val first = it.args.component1() as Int
            val second = it.args.component2() as Int

            it.respond("${first + second}")
        }
    }
}
package me.aberrantfox.kjdautils.examples


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.KJDAConfiguration
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.ConversationService
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.command.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.listeners.ConversationListener
import net.dv8tion.jda.core.entities.TextChannel
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
        val conversationService = ConversationService(jda, config)
        conversationService.registerConversations("me.aberrantfox.kjdautils")

        registerInjectionObject(myConfig, myLog)
        registerInjectionObject(conversationService, config)
        registerCommands(commandPath, prefix)
        registerListenersByPath("me.aberrantfox.kjdautils.examples")
        registerListeners(ConversationListener(conversationService))

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
fun commandSet(myConfig: MyCustomBotConfiguration, log: MyCustomLogger, conversationService: ConversationService) = commands {
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

    command("guildowner") {
        description = "Provide info about the guild you executed the command in"
        execute {
            //This command just won't do anything if it's executed in DM. You may want to send a response.
            val guild = it.guild ?: return@execute
            it.respond("${guild.name} is owned by ${guild.owner}")
        }
    }

    command("conversationtest") {
        description = "Test the implementation of the ConversationDSL"
        execute {
            val eventChannel = it.channel as TextChannel
            conversationService.createConversation(it.author.id, eventChannel.guild.id, "test-conversation")
        }
    }
}

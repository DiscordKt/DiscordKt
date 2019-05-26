package me.aberrantfox.kjdautils.examples


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.annotation.Data
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.ConversationService
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.command.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.command.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

data class MyCustomBotConfiguration(val version: String, val token: String)

data class MyCustomLogger(val prefix: String) {
    fun log(data: String) = println(data)
}

fun main(args: Array<String>) {
    val token = args.component1()

    startBot(token) {
        val myConfig = MyCustomBotConfiguration("0.1.0", token)
        val myLog = MyCustomLogger(":: BOT ::")

        registerInjectionObject(myConfig, myLog)

        configure {
            prefix = "!"
            globalPath = "me.aberrantfox.kjdautils.examples"
        }

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

    command("guildsize") {
        description = "Display how many members are in a guild"
        requiresGuild = true
        execute {
            it.respond("There are ${it.guild!!.members.size} members ")
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
        requiresGuild = true
        execute {
            conversationService.createConversation(it.author.id, it.guild!!.id, "test-conversation")
        }
    }
}

@CommandSet
fun defineOther(log: MyCustomLogger) = commands {
    command("someCommand") {
        execute { log.log("Hello, World!") }
    }
}


@Precondition
fun nameBeginsWithF() = precondition {
    if(it.author.name.toLowerCase().startsWith("f")) {
        return@precondition Pass
    } else {
        return@precondition Fail("Your name must start with F!")
    }
}

@Service
class NoDependencies

@Service
class SingleDependency(noDependencies: NoDependencies)

@Service
class DoubleDependency(noDependencies: NoDependencies, singleDependency: SingleDependency)

@CommandSet("services-demo")
fun dependsOnAllServices(none: NoDependencies, single: SingleDependency, double: DoubleDependency) = commands {
    command("dependsOnAll") {
        description = "I depend on all services"
        execute {
            it.respond("This command is only available if all dependencies were correctly piped to the wrapping function")
        }
    }
}


@Data("config.json")
data class ConfigurationObject(var prefix: String = "!")

@CommandSet
fun dependsOnAboveDataObject(config: ConfigurationObject, persistenceService: PersistenceService) = commands {
    command("data-see") {
        description = "This command depends on the data object above, which is automatically loaded from the designated path." +
                "if it does not exist at the designated path, it is created using the default arguments."
        execute {
            it.respond(config.prefix)
        }
    }
    command("data-save") {
        description = "This command tests the save command"
        execute {
            config.prefix = "different"
            persistenceService.save(config)
        }
    }
}

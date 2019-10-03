package me.aberrantfox.kjdautils.examples


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import me.aberrantfox.kjdautils.internal.services.ConversationService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

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
            documentationSortOrder = listOf("Data", "ServicesDemo", "Misc", "Utility")
            mentionEmbed = embed {
                title = "Hello World!"
            }
        }
    }
}

class MessageLogger(val myConfig: MyCustomBotConfiguration) {
    @Subscribe
    fun onMessage(event: GuildMessageReceivedEvent) {
        println("ExampleBot :: V${myConfig.version} :: ${event.message.contentRaw}")
    }
}

@CommandSet("Utility")
fun commandSet(myConfig: MyCustomBotConfiguration, log: MyCustomLogger, conversationService: ConversationService) = commands {
    command("DisplayMenu") {
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

    command("DisplayEmbed") {
        description = "Display an example embed."
        execute {
            it.respond(
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
            )
        }
    }

    command("Version") {
        description = "A command which will show the version."
        execute {
            it.respond(myConfig.version)
            log.log("Version logged!")
        }
    }

    command("Echo") {
        expect(SentenceArg)
        execute {
            val response = it.args.component1() as String
            it.respond(response)
        }
    }

    command("Add") {
        description = "Add two numbers together"
        expect(IntegerArg, IntegerArg)
        execute {
            val first = it.args.component1() as Int
            val second = it.args.component2() as Int

            it.respond("${first + second}")
        }
    }

    command("OptionalAdd") {
        description = "Add two numbers together"
        expect(arg(IntegerArg, false), arg(IntegerArg, true, 1))
        execute {
            val first = it.args.component1() as Int
            val second = it.args.component2() as Int

            it.respond("${first + second}")
        }
    }

    command("OptionalInput") {
        description = "Optionally input some text"
        expect(arg(SentenceArg, optional = true))
        execute {
            val sentence = it.args.component1() as String? ?: "<No input>"

            it.respond("Your input was: $sentence")
        }
    }

    command("GuildSize") {
        description = "Display how many members are in a guild"
        requiresGuild = true
        execute {
            it.respond("There are ${it.guild!!.members.size} members ")
        }
    }

    command("GuildOwner") {
        description = "Provide info about the guild you executed the command in"
        execute {
            //This command just won't do anything if it's executed in DM. You may want to send a response.
            val guild = it.guild ?: return@execute
            it.respond("${guild.name} is owned by ${guild.owner}")
        }
    }

    command("ConversationTest") {
        description = "Test the implementation of the ConversationDSL"
        requiresGuild = true
        execute {
            conversationService.createConversation(it.author.id, it.guild!!.id, "test-conversation")
        }
    }
}

@CommandSet("Misc")
fun defineOther(log: MyCustomLogger) = commands {
    command("SomeCommand") {
        execute { log.log("Hello, World!") }
    }
}

@Precondition
fun nameBeginsWithLetter() = precondition {
    if(it.author.name.toLowerCase().first() in 'a'..'z') {
        return@precondition Pass
    } else {
        return@precondition Fail("Your name must start with a letter!")
    }
}

@Precondition(priority = 3)
fun userWithDiscriminator() = precondition {
    return@precondition if(it.author.discriminator == "5822") {
        Fail("Ignoring users with your discriminator.")
    } else {
        Pass
    }
}

@Precondition(priority = 1)
fun userWithID() = precondition {
    return@precondition if (it.author.id == "140816962581299200") {
        Fail()
    } else {
        Pass
    }
}

@Service
class NoDependencies

@Service
class SingleDependency(noDependencies: NoDependencies)

@Service
class DoubleDependency(noDependencies: NoDependencies, singleDependency: SingleDependency)

@CommandSet("ServicesDemo")
fun dependsOnAllServices(none: NoDependencies, single: SingleDependency, double: DoubleDependency) = commands {
    command("DependsOnAll") {
        description = "I depend on all services"
        execute {
            it.respond("This command is only available if all dependencies were correctly piped to the wrapping function")
        }
    }
}


@Data("config.json")
data class ConfigurationObject(var prefix: String = "!")

@CommandSet("Data")
fun dependsOnAboveDataObject(config: ConfigurationObject, persistenceService: PersistenceService) = commands {
    //This command depends on the data object above, which is automatically loaded from the designated path.
    //If the file does not exist at the designated path, it is created using the default arguments.
    command("DataSee") {
        description = "This command demonstrates loading and injecting Data objects by viewing its contents."
        execute {
            it.respond(config.prefix)
        }
    }
    command("DataSave") {
        description = "This command lets you modify a Data object's contents."
        execute {
            config.prefix = "different"
            persistenceService.save(config)
        }
    }
}
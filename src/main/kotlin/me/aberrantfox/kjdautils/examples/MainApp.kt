package me.aberrantfox.kjdautils.examples


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.*
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
            documentationSortOrder = listOf("Data", "ServicesDemo", "Misc", "Utility")
            mentionEmbed = { event ->
                embed {
                    val name = event.guild.name

                    title = "Hello World!"
                    description = "I was mentioned in $name"
                }
            }
        }

        registerCommandPreconditions({
            if (it.channel.name != "ignored") {
                Pass
            } else {
                Fail()
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
        execute(SentenceArg) {
            val response = it.args.component1()
            it.respond(response)
        }
    }

    command("Add") {
        description = "Add two numbers together"
        execute(IntegerArg, IntegerArg) {
            val (first, second) = it.args
            it.respond("${first + second}")
        }
    }

    command("OptionalAdd") {
        description = "Add two numbers together"
        execute(IntegerArg, IntegerArg.makeOptional(5)) {
            val (first, second) = it.args
            it.respond("${first + second}")
        }
    }

    command("OptionalInput") {
        description = "Optionally input some text"
        execute(SentenceArg.makeNullableOptional()) {
            val sentence = it.args.component1() ?: "<No input>"
            it.respond("Your input was: $sentence")
        }
    }

    command("NumberOrWord") {
        description = "Enter a word or a number"
        execute(IntegerArg or WordArg) {
            when (val input = it.args.first) {
                is Either.Left -> it.respond("You input the number: ${input.left}")
                is Either.Right -> it.respond("You input the word: ${input.right}")
            }
        }
    }

    command("Sum") {
        description = "Sum a set of numbers"
        execute(MultipleArg(IntegerArg, "Numbers").makeOptional(listOf(0))) {
            val numbers = it.args.component1()
            val sum = numbers.sum()
            it.respond("Sum: $sum")
        }
    }

    command("ConversationTest") {
        description = "Test the implementation of the ConversationDSL"
        requiresGuild = true
        execute {
            conversationService.createConversation(it.author, it.guild!!, "test-conversation")
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
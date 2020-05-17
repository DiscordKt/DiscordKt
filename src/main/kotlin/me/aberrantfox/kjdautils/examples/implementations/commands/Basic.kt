package me.aberrantfox.kjdautils.examples.implementations.commands

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.*

//Commands are the main interface that will be used to interact with your bot.
//This is a good place to start understanding how commands are written.

@CommandSet("Basic")
fun basicCommands() = commands {
    //This command accepts no arguments and just sends back some text
    command("Hello") {
        description = "Display a simple message."
        execute {
            it.respond("Hello World")
        }
    }

    //Commands can also have multiple names and be reference by any of the,
    command("Version", "V") {
        description = "Display the version."
        execute {
            it.respond(it.discord.properties.kutilsVersion)
        }
    }

    //This command accepts some text of any length and sends it back
    command("Echo") {
        description = "Echo some text back."
        execute(EveryArg) {
            val response = it.args.first
            it.respond(response)
        }
    }

    //This command accepts 2 integers and replies with their sum
    command("Add") {
        description = "Add two numbers together."
        execute(IntegerArg, IntegerArg) {
            //The args bundle can be destructured like this, or by using first/second
            val (first, second) = it.args
            it.respond("${first + second}")
        }
    }
}
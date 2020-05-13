package me.aberrantfox.kjdautils.examples.implementations

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.*

//Commands are the main interface that will be used to interact with your bot.
//This is a good sample of commands that accept arguments of different types.

@CommandSet("Command Demo")
fun demoCommands() = commands {
    //This command accepts no arguments and just sends back some text
    command("Hello") {
        description = "Display a simple message."
        execute {
            it.respond("Hello World")
        }
    }

    //This command accepts some text of any length and sends it back
    command("Echo") {
        description = "Echo some text back."
        execute(SentenceArg) {
            val response = it.args.first
            it.respond(response)
        }
    }

    //This command accepts 2 integers and replies with their sum
    command("Add") {
        description = "Add two numbers together."
        execute(IntegerArg, IntegerArg) {
            val (first, second) = it.args
            it.respond("${first + second}")
        }
    }

    //This command accepts between 1-2 args, as the second arg is optional with a default value of 5
    command("OptionalAdd") {
        description = "Add two numbers together."
        execute(IntegerArg, IntegerArg.makeOptional(default = 5)) {
            val (first, second) = it.args
            it.respond("${first + second}")
        }
    }

    //This command accepts any number of integers and replies with their sum
    command("Sum") {
        description = "Add a list of numbers together."
        execute(MultipleArg(IntegerArg).makeOptional(listOf(0))) {
            val numbers = it.args.first
            it.respond("Total: ${numbers.sum()}")
        }
    }

    //This command accepts an optional user and exposes the command event
    command("User") {
        description = "Display a user's full name."
        execute(UserArg.makeOptional { it.author }) {
            val user = it.args.first
            it.respond(user.fullName())
        }
    }

    //This command accepts EITHER a number or a word
    command("NumberOrWord") {
        description = "Enter a number or a word."
        execute(IntegerArg or WordArg) {
            val input = it.args.first

            val result = input.getData(
                { "You input the number: $it" },
                { "You input the word: $it" }
            )

            it.respond(result)
        }
    }

    //This command accepts no text arguments but will check the message for a file attachment
    command("File") {
        description = "Input a file and display its name."
        execute(FileArg) {
            val file = it.args.first
            it.respond(file.name)
        }
    }
}
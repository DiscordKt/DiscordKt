package me.aberrantfox.kjdautils.examples.implementations.commands

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.arguments.*

//These commands showcase some of the more complicated ArgumentTypes and how to use them.

@CommandSet("Special")
fun specialCommands() = commands {
    //This command accepts any number of integers and replies with their sum
    command("Sum") {
        description = "Add a list of numbers together."
        execute(MultipleArg(IntegerArg)) {
            val numbers = it.args.first
            it.respond("Total: ${numbers.sum()}")
        }
    }

    //This command accepts EITHER a number or a word
    command("NumberOrWord") {
        description = "Enter a number or a word."
        execute(IntegerArg or AnyArg) {
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
        execute(FileArg, IntegerArg) {
            val file = it.args.first
            it.respond(file.name)
        }
    }
}
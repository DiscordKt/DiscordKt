package me.jakejmattson.kutils.internal.examples.implementations.commands

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.*
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.services.ScriptEngineService

//These commands showcase some of the more complicated ArgumentTypes and how to use them.

@CommandSet("Special")
fun specialCommands(scriptingEngine: ScriptEngineService) = commands {
    //This command accepts any number of integers and replies with their sum
    command("Sum") {
        description = "Add a list of numbers together."
        execute(MultipleArg(IntegerArg)) {
            val numbers = it.args.first
            it.respond("Total: ${numbers.sum()}")
        }
    }

    //This command accepts EITHER a number or any input
    command("Either") {
        description = "Enter a number or any input."
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
        execute(FileArg) {
            val file = it.args.first
            it.respond(file.name)
        }
    }

    command("Eval") {
        description = "Evaluate a Kotlin expression."
        execute(EveryArg("Script")) {
            val input = it.args.first
            val evalResult = scriptingEngine.evaluateScript(input)
            it.respond(evalResult.toString())
        }
    }
}
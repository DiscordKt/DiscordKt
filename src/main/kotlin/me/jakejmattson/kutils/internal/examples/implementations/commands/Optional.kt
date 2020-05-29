package me.jakejmattson.kutils.internal.examples.implementations.commands

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.arguments.*
import me.jakejmattson.kutils.api.dsl.command.commands
import me.jakejmattson.kutils.api.extensions.jda.fullName

//This shows you how to make certain arguments optional and how to set default values.

@CommandSet("Optional")
fun optionalCommands() = commands {
    //This command accepts one integer, and optionally a second integer; if none is provided, the default is used
    command("OptionalAdd") {
        description = "Add two numbers together."
        execute(IntegerArg, IntegerArg.makeOptional(default = 5)) {
            val (first, second) = it.args
            it.respond("${first + second}")
        }
    }

    //This command accepts a user, but sets the user who triggered this command as the default if no args are provided
    command("User") {
        description = "Display this user's full tag."
        execute(UserArg.makeOptional { it.author }) {
            val user = it.args.first
            it.respond(user.fullName())
        }
    }

    //Default values can also support nullability if null is a possible result
    command("Null") {
        description = "Display the first element in a list."
        execute(AnyArg.makeNullableOptional(null)) {
            val element = it.args.first ?: "No elements"
            it.respond(element)
        }
    }

    //You may also want to grab a Discord entity that fail and result in null value
    command("Guild") {
        description = "Display the current guild name."
        execute(GuildArg.makeNullableOptional { it.guild }) {
            val element = it.args.first?.name ?: "No elements"
            it.respond(element)
        }
    }
}
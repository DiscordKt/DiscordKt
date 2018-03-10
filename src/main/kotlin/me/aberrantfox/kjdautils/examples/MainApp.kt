package me.aberrantfox.kjdautils.examples

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.CommandSet


fun main(args: Array<String>) {
    val token = args.component1()
    val prefix = "!"
    val commandPath =  "me.aberrantfox.kjdautils.examples"

    startBot(token, prefix, commandPath)
}

@CommandSet
fun helpCommand() = commands {
    command("help") {
        execute {
            it.respond(embed {
                title("Help menu")
                description("Below you can see how to use all of the commands in this startBot")

                field {
                    name = "Help"
                    value = "Display a help menu"
                }

                field {
                    name = "Ping"
                    value = "Pong"
                }

                field {
                    name = "Echo"
                    value = "Echo the command arguments in the current channel."
                }
            })
        }
    }

    command("ping") {
        execute {
            it.respond("Pong!")
        }
    }

    command("echo") {
        expect(ArgumentType.Sentence)
        execute {
            val response = it.args.component1() as String
            it.respond(response)
        }
    }
}
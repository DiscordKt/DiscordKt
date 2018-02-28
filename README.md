##### Purpose
The purpose of this is to provide a nice kotlin wrapper over JDA and to add a bunch of extension functions for utility 
purposes/cleaner code.

##### Note
This is still in a very alpha stage, this section of the README will be updated with a maven import when it is ready 
for public use.

sample bot:
```kotlin
fun main(args: Array<String>)  =
    startBot("token", "ownerId", "!", "guildID", "me.aberrantfox.epicbot.commands") {
        registerCommandPrecondition {
            it.author.id == config.ownerID
        }
    }

fun otherMain() {
    val sample = startBot("token", "ownerId", "!", "guildID", "me.aberrantfox.epicbot.commands")
    sample.registerCommandPrecondition {
        it.author.toMember(it.guild).isOwner
    }
}

//in any file in the package me.aberrantfox.epicbot.commands
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
```
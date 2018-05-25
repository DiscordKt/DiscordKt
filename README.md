##### Purpose
The purpose of this is to provide a nice kotlin wrapper over JDA and to add a bunch of extension functions for utility 
purposes/cleaner code.

##### Key Features

- A very powerful, flexible command DSL for making testable commands.
- A means for event handling that doesn't require inheritance, using Guava event bus
- A means to mock out your dependencies for each component, allowing you to test as you please.
- An embed DSL
- No need to worry about blocking commands, they are all automatically wrapped into a coroutine context. 

##### Sample Bot

```kotlin
data class MyCustomBotConfiguration(val version: String , val token: String)

data class MyCustomLogger(val prefix: String) {
    fun log(data: String) = println(data)
}

fun main(args: Array<String>) {
    val token = args.component1()
    val prefix = "!"
    val commandPath =  "me.aberrantfox.kjdautils.examples"

    startBot(token) {
        val myConfig = MyCustomBotConfiguration("0.1.0", token)
        val myLog = MyCustomLogger(":: BOT ::")
        registerInjectionObject(myConfig, myLog)
        registerCommands(commandPath, prefix)
        registerListener(MessageLogger())
    }
}

class MessageLogger {
    @Subscribe fun onMessage(event: GuildMessageReceivedEvent) = println(event.message.contentRaw)
}

@CommandSet
fun defineOther(log: MyCustomLogger) = commands {
    command("someCommand") {
        execute { log.log("Hello, World!") }
    }
}

@CommandSet
fun helpCommand(myConfig: MyCustomBotConfiguration, log: MyCustomLogger) = commands {
    command("version") {
        execute {
            it.respond(myConfig.version)
            log.log("Version logged!")
        }
    }
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

**For a more comprehensive guide, see the Wiki** 
 https://github.com/AberrantFox/KUtils/wiki


#### Add to your project with Maven
Under the dependencies tag, add

```xml
<dependency>
    <groupId>com.github.aberrantfox</groupId>
    <artifactId>Kutils</artifactId>
    <version>0.4.0</version>
</dependency>
```

Under the repositories tag, add

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

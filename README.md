##### Purpose
The purpose of this is to provide a nice kotlin wrapper over JDA and to add a bunch of extension functions for utility 
purposes/cleaner code.

##### Key Features

- A very powerful, flexible command DSL for making testable commands.
- A means for event handling that doesn't require inheritance, using Guava event bus
- A means to mock out your dependencies for each component, allowing you to test as you please.
- An embed DSL
- No need to worry about blocking commands, they are all automatically wrapped into a coroutine context. 

##### Examples
*Note: For these examples, the token being obtained is ommited*

**Ping bot**
```kotlin
fun main(args: Array<String>) {
    startBot(token) {
        val commandPath = "me.awesomebot.commandspackage"
        val prefix = "!"
        registerCommands(commandPath, prefix)
    }
}

//in any kotlin file in the package me.awesomebot.commandspackage:
@CommandSet
fun createSomeCoolCommands() = commands {
    command("ping") {
        execute {
            it.respond("Pong!")
        }
    }
}
```

**Listen to an event**
```kotlin
startBot(token) {
    registerEventListeners(MessageLogger())
}

class MessageLogger {
    @Subscribe fun onMessage(event: GuildMessageReceivedEvent) = println(event.message.contentRaw)
}
```


**A CommandSet with a dependency**
```kotlin
@CommandSet
fun createConfigCommands(config: Configuration) = commands {
    command("config") {
        execute {
            it.respond(config)
        }
    }
}

//notice how that command set took a configuration object? 

data class Configuration(val botName: String = "Jeff")

//well, in your startbot function, you can pass around an instance of a class to all of your commandsets:
startBot(token) {
    //that config will now be passed to our commandSet defined above.
    registerInjectionObject(Configuration())
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
    <version>0.5.0</version>
</dependency>
```

Under the repositories tag, add

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

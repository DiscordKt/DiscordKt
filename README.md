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


**Create a command with some arguments**
```kotlin
@CommandSet
fun createConfigCommands(config: Configuration) = commands {
    command("mention") {
        //Here we say that we expect a User as an argument
        expect(UserArg)
        execute {
            //Since we said we expect to have a user as an argument, 
            //this code will only happen if one is supplied. Meaning it is
            //safe to just pull it out of the argument array and cast.
            val user = it.args.component1() as User
            it.respond(user.asMention)
        }
    }
}
```

**Create a custom command Argument**

Parsing the same thing over and over again is very annoying, that's why the expect function 
exists. But there is a problem, what if you wanted a custom command argument? Well the parser
supports that.

```kotlin
object ChoiceArg : ArgumentType {
    //Is the argument just a word, a few words, or the entire string? (Single, Multiple or All)
    override val consumptionType = ConsumptionType.Single
    //Okay, define a means to determine if a string contains a valid instance of this argumentType
    override fun isValid(arg: String, event: CommandEvent) = arg.isBooleanValue()
    //Now, define a conversion function.
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = ArgumentResult.Single(arg.toBooleanValue())
}
```

This is how the ChoiceArgument is defined. You don't need to redefine this, it comes with the library. 
But this ability to just define your own arguments will save you a lot of repeated parsing. 


**Utilize command pre-conditions**
Command preconditions are predicate or boolean expressions that must all evaluate to true before commands are allowed to execute. So if you want to ignore the commands of a particular user, or if you want to create a commands permission system, you might use this. 

```kotlin
registerCommandPreconditions({
    if (it.author.discriminator == "3698") {
        Fail("Ignoring users with your discriminator.")
    } else {
        Pass
    }
})
```

The above example will make it so that anyone with the defined discriminator will not be able to use commands, and they get the message passed to Fail() as the reason why. 


**For a more comprehensive guide, see the Wiki** 
 https://github.com/AberrantFox/KUtils/wiki


#### Add to your project with Maven
Under the dependencies tag, add

```xml
<dependency>
    <groupId>com.github.aberrantfox</groupId>
    <artifactId>Kutils</artifactId>
    <version>0.6.0</version>
</dependency>
```

Under the repositories tag, add

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

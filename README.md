##### Purpose
The purpose of this is to provide a nice kotlin wrapper over JDA and to add a bunch of extension functions for utility 
purposes/cleaner code.

#### Simple sample bot
```kotlin
fun main(args: Array<String>) {
    //get the token out of the command line arguments
    val token = args.first()
    
    //call the start procedure to boot up the bot
    startBot(token) {
        //configure where commands and the like can be found within the project.
        configure {
            prefix = "!"
            //this should just be the path to all of your code.
            globalPath = "me.sample.commands"
        }
    }
}


//in any .kt file inside of the package me.sample.commands

//The command set annotation flags the function for calling by the command executor.
//the "utility" string is the category of commands. 
//!help utility - This will list all of the commands inside of the utility commandset (i.e. any command { } definition
//from the below function
@CommandSet("utility")
fun createUtilityCommands() = commands {
    //declare a command, which can be invoked by using !ping
    command("ping") {
        //the decription displayed when someone uses !help ping
        description = "Instruct the bot to send back a 'Pong!' message, useful for checking if the bot is alive"
        //the code that is run when the command is invoked
        execute { commandEvent ->
            //respond to wherever the commandEvent came from with the message "Pong!"
            commandEvent.respond("Pong!")
        }
    }
    
    command("add") {
        description = "Add two numbers together, and display the result"
        //this simply states that the command expects two doubles; What the expect function requires is applied in the 
        //same order the command is invoked, so:
        //       first     second
        expect(DoubleArg, DoubleArg)
        execute { commandEvent ->
            //if your code gets here you can freely cast the arguments to the output of the Argument, in this case
            //DoubleArg outputs a Double value!
            val first = it.args.component1() as Double 
            val second = it.args.component2() as Double
            
            //respond with the result
            it.respond("The result is ${first + second}")
        }
    }
}

@Precondition
fun nameBeginsWithF() = precondition {
    if(it.author.name.toLowerCase().startsWith("f")) {
        return@precondition Pass
    } else {
        return@precondition Fail("Your name must start with F!")
    }
}
@Service
class NoDependencies

@Service
class SingleDependency(noDependencies: NoDependencies)

@Service
class DoubleDependency(noDependencies: NoDependencies, singleDependency: SingleDependency)

@CommandSet("services-demo")
fun dependsOnAllServices(none: NoDependencies, single: SingleDependency, double: DoubleDependency) = commands {
    command("dependsOnAll") {
        description = "I depend on all services"
        execute {
            it.respond("This command is only available if all dependencies were correctly piped to the wrapping function")
        }
    }
}

@Data("config.json")
data class ConfigurationObject(val prefix: String = "!")

@CommandSet
fun dependsOnAboveDataObject(config: ConfigurationObject) = commands {
    command("data-test") {
        description = "This command depends on the data object above, which is automatically loaded from the designated path." +
                "if it does not exist at the designated path, it is created using the default arguments."
        execute {
            it.respond(config.prefix)
        }
    }
}
```


#### Index

1. Starting up the bot and basic configuration
2. Creating commands
3. Command arguments
4. Listening to events
5. Creating and using conversations
6. Creating and using a Service
7. Creating and using auto-injected data objects


#### Starting up the bot and basic configuration

##### Starting up
The start up procedure for KUtils is very simple, you just need to provide the token in order to get going. This is the 
minimal startup code required: 

```kotlin
fun main(args: Array<String>) {
    val token = "a-real-bot-token-no-really"
    startBot(token) {
        //the bot is now started and running
    }
}
```

Of course, you can't do much with it just yet. You need to configure it just a little bit before it will be of much use.

##### Configuration

When inside the `startBot` block, there is a `configure` block, in which you can set the `prefix`, which is used to 
invoke commands, and the `globalPath`. 

###### Prefix
The prefix is pretty self-explanatory, consider the below usage of a command:
`!Ban Tim Being a bad user.`

In this case, the prefix is `!`. You can freely set this to any string of any length. The bot also comes equipped with
several kinds of prefix behaviour. These are configurable by setting `deleteMode`. This is an enum which can have three 
values:
 - Single - invoking commands with a single `!` will cause the command message to be deleted, but invoking with `!!` 
   (the prefix repeated twice) will result in the message staying. 
 - Double - The inverse of Single
 - None - Neither double or single prefixing will delete the message.

###### GlobalPath
The `globalPath` variable is just a stirrup for the rest of the framework, it uses this to quickly read out and create
all of your services, commands, etc. You should set it to the root package of your project.  E.g. if you have the 
following folder structure:

```
-- me.bob.mycoolbot
   -- commands
      -- AdminCommands.kt
      -- ModeratorCommands.kt
      -- UtilityCommands.kt
   -- listeners
      -- SpamListener.kt
   -- conversations
      -- SetupConversation.kt
-- Main
```

The `root` as mentioned earlier in *this* example is `me.bob.mycoolbot`, as it is the highest directory containing all
the code.

#### Creating commands

##### Basics
The command framework in KUtils is quite complex, but it provides a hell of a lot of free functionality for very little 
code, so it is strongly recommend to learn how to use it as it will save you a lot of time in the future.

The way you register commands with KUtils is by using the `@CommandSet` annotation. This annotation has one property, 
`category`, which is a string. Here is an example command set. 

```kotlin
@CommandSet("utility-commands")
fun createTheUtilityCommandSet() = commands {
    command("ping") {
        description = "Responds with pong!"
        execute {
            it.respond("pong!")
        }
    }
}
```

Here, the `category` was set to `utility-commands`. This means that if you use !help utility-commands, you will be 
presented with a list of commands in that category. In this case, just `ping`. If you use !help ping, you will receive
a lot of information about the ping command. You will see that this command takes no arguments, and you will also 
be provided with the description, as well as an example of someone using the command. Try it out for yourself!

##### Commands with Arguments 

Sometimes you need to define a command that will have extra information provided with it, like for example, a ban command.
You can do that with KUtils, it will allow you to create commands that take as many arguments in many different ways,
and it will do all of the checks for you. For example, let's take a look at a simple `add` command, this will take 
2 double values, and then respond with the result.

```kotlin
@CommandSet("demo")
fun addCommand() = commands {
    command("add") {
        description = "I will return the result of adding the two provided numbers."
        expect(DoubleArg, DoubleArg)
        execute {
            val first = it.args.component1() as Double
            val second = it.args.component2() as Double
            
            it.respond("${first + second} is the result")
        }
    }
}
```

Some things to note: 
 - The `expect` function said that this command expects a `DoubleArg` and then another `DoubleArg`. No additional 
   information, like if the argument is optional or not, was provided, so the command assumes it is required.
 - The execute function accesses the `args` array and then casts the values at the first and second indexes to Double.
   You can safely do this because the library will not allow any values in that don't pass the checks provided by 
   `DoubleArg`
 - The result is then responded to the user as normal.
 - The help function will be able to generate example usage of this command despite requiring more arguments. Try 
   if for yourself. :) 
   

#### Creating arguments

So you seen before that there was a DoubleArg, you might be wondering, what other arguments are there? Well, you 
can see the fully exhaustive list below. 

##### Available Arguments

 - **ChannelCategoryArg** - Accepts Command categories
 - **ChoiceArg** - Accepts true, or false. You can also provide it with a list of arguments to accept via the constructor,
   e.g. `ChoiceArg("YesNoArg", "Y", "y", "N", "n")` note: the first arg in every constructor is always the `name` that 
   is shown in the `help` command.
 - **CommandArg** - Accepts any command as an argument.
 - **DoubleArg** - Accepts any valid double
 - **IntegerArg** - Accepts any valid integer
 - **ManualArg** - be VERY mindful using this argument type, it turns off all parsing and leaves it to you!
 - **RoleArg** - Accepts any given RoleID, useful for administrators. 
 - **SentenceArg** - Accepts multiple words worth of strings, e.g `!something a b c`, `a b c` would be a valid sentence arg.
   So would just `a`, though.
 - **SplitterArg** - Takes all of the arguments and returns a list split by a delimiter, `|`. E.g. `first sentence | second sentece ...`
 - **TextChannelArg** - Accepts any text channel ID as an argument.
 - **TimeStringArg**- Accepts timestrings, e.g:
    - 1d 10h 5m 
    - 1 day 10 hours 5 minutes
    - 12 hours 10minutes 3s
 - **UrlArg** - Accepts any URL matching a provided regex, you may want to craft your own UrlArg or use a SentenceArg if you
   use some really funky looking URLs
 - **UserArg** - Accepts any user ID or mention. 
 - **VoiceChannelArg** - Accepts any voice channel ID
 - **WordArg** - Accepts just a single word.

##### Creating your own argument

You might be wondering at this point how do you create your own argument. The point of CommandArguments is to get around
having all of these ugly checks inside of commands repeatedly. Consider the following example: You are working on a 
blackjack bot, say you want to make it simple and you just want to make it so that users can `hit` or `stand`. 

Well, you could do the very annoying thing and have these checks inside your `execute` block (*which is bothersome 
because if you need to do these checks basically anywhere else you are duplicating code*) and then accept a WordArg. Or, 
you could make your own, like so:

```kotlin
open class BlackJackMoveArg(override val name : String = "BlackJackMove") : ArgumentType {
    companion object : BlackJackMoveArg()

    override val examples = arrayListOf("hit", "stand")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) {
        val lowerArg = arg.toLowerCase()
        
        return if(examples.contains(lowerArg)) {
            Pass
        } else {
            Fail("Valid options are `hit` or `stand`")
        }
    }
}
```

That's a big chunk of code to consider, so let's break it down piece by piece. 
Firstly, you must extend ArgumentType in order for the `expect` function to accept the ArgumentType. 

Second, the companion object extends and instantiates an instance of `BlackJackMove`. This allows you to add 
`BlackJackMoveArg`s to the expect function in two ways: 
 - `expect(BlackJackMoveArg)`
 - `expect(BlackJackMoveArg("customName"))`

the `customname`, which will show up in the !help command is not super useful for `BlackJackMoveArg`, but it is very 
useful for things like `WordArg`, as you can help provide direction for what you want the user to put in there.

Third, once you have extended properly, and you've decided on a human readable name (provide a default value for it),
you must provide some examples. These are what the framework uses to generate help documentation. The more examples you
provide, the better, as a random one is selected each time help for a command which uses this is called. 

Fourth, you now must provide a ConsumptionType and a Convert function override. The **output** of the convert function
depends on the **ConsumptionType** so pay attention to how this works! If you are just taking one word from the command,
you should use `ConsumptionType.Single`. If you are using the rest of the string, you should use `ConsumptionType.All`.
If you are using every element until a condition fails, you should use `ConsumptionType.Multiple`.

Examples:
 - `Single` - `WordArg`, just takes one single word
 - `All` - `SentenceArg`, takes the remainder of the arguments
 - `Multiple`, - `TimeStringArg`, takes each argument until it's not a valid timeString anymore.

In order to really understand, you'll have to play around with it. Just note that you can't use multiple `All` functions
together. 

Finally, once you have decided on your consumption type and all of the cursory information which indicates what kind of 
argument this is, you must now provide the checks that see if a given string (or strings) is a valid argument or not.

In the case of the `BlackJackMoveArg`, I just see if the given `arg` is in the `examples` I provided. If it is, I 
return `Pass`, else I return `Fail` equipped with a message that indicates what went wrong. 

The command using this will then break out of that invocation before it even hits the execute block and it will provide
this error message to the user invoking it.

< Rest of the documentation is a work in progress and is on the way > 

#### Add to your project with Maven
Under the dependencies tag, add

```xml
<dependency>
    <groupId>com.gitlab.aberrantfox</groupId>
    <artifactId>Kutils</artifactId>
    <version>0.9.11</version>
</dependency>
```

Under the repositories tag, add

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

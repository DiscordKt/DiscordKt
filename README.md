##### Purpose
The purpose of this is to provide a nice kotlin wrapper over JDA and to add a bunch of extension functions for utility 
purposes/cleaner code.

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

 - **BooleanArg** - Accepts a boolean `true` or `false` (case insensitive)
 - **CategoryArg** - Accepts Command categories
 - **CharArg** - Accepts a single character
 - **ChoiceArg** - Accepts true, or false. You can also provide it with a list of arguments to accept via the constructor,
   e.g. `ChoiceArg("YesNoArg", "Y", "y", "N", "n")` note: the first arg in every constructor is always the `name` that 
   is shown in the `help` command.
 - **CommandArg** - Accepts any command as an argument.
 - **DoubleArg** - Accepts any valid double
 - **GuildArg** - Accepts a guild ID
 - **HexColorArg** - Accepts any valid hex color
 - **IntegerArg** - Accepts any valid integer
 - **IntegerRangeArg** - Accepts a valid integer within a range
 - **ManualArg** - be VERY mindful using this argument type, it turns off all parsing and leaves it to you!
 - **MemberArg** - Accepts a member ID or mention
 - **MessageArg** - Accepts a message ID
 - **OnOffArg** - Accepts `on` or `off` (case insensitive)
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
 - **YesNoArg** - Accepts `yes` or `no` (case insensitive)

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

##### Additional command options

There are some more tricks to commands that will help you achieve the kind of functionality you want. For example,
you can set it such that a command requires being invoked in a guild by setting `requiresGuild` to true on the `command`
block. 

You also have the option to add generic checks prior to all command invocations. If you wanted to build your own permission
system, you would need this. 


```kotlin
@Precondition
fun nameBeginsWithF() = precondition {
    if(it.author.name.toLowerCase().startsWith("f")) {
        return@precondition Pass
    } else {
        return@precondition Fail("Your name must start with F!")
    }
}
```

Above, you can see a simple usage of preconditions. You can see that it checks if the CommandEvent Author's name starts 
with an F. If it does, they can use the commands (any command, this check runs before them all). If not, they will 
get the error message. You can easily use this to make it so that only people with your UserID are able to use the commands.
You could also make it so that only IDs within a file or database table can use commands using preconditions.


##### Making arguments optional

As a final note on command arguments, you can also making them optional like so:


```kotlin
@CommandSet("sample")
fun optionalArgExample() = commands {
    command("add") {
        description = "Add two numbers together"
        expect(arg(IntegerArg, false), arg(IntegerArg, true, 1))
        execute {
            val first = it.args.component1() as Int
            val second = it.args.component2() as Int
            
            it.respond("${first + second}")
        }
    }
}
```

You can see the second argument in our `add` command here is now optional (as seen by the `true` value in the `arg` method),
and it sets the default value to `1`. This means that this command can have 1 or 2 arguments instead of just 2.

#### Listening to events

Listening to discord events basically couldn't be any easier. KUtils drops and removes the old ListenerAdapter method 
and instead presents the Guava EventBus as a means to hook into events. This is done using the `@Subscribe` annotation.

Simply annotate *any* method in *any* class that accepts the event you want to listen to. It is recommend to put listeners
in their own package to keep things clean. You can see an example here:

```kotlin
class MessageLogger {
    @Subscribe
    fun onMessage(event: GuildMessageReceivedEvent) {
        println("ExampleBot :: V${myConfig.version} :: ${event.message.contentRaw}")
    }
}
```

You can see that this class has a function which is subscribed to the GuildMessageReceivedEvent, you can see a full list
of events directly [here](https://github.com/DV8FromTheWorld/JDA/wiki/8%29-List-of-Events), you may subscribe to all of
these as you wish. 


#### Creating and using conversations

Sometimes you need commands which are many times more robust than what the current system offers. CommandArguments 
are very useful, but if you need a huge amount of information from the user they can seem limited. This is where 
the conversation dsl comes in, it's a very useful means for collecting much more information over many messages. 

Here is an example conversation: 


```kotlin
fun testConversation() = conversation {
    name = "test-conversation"
    description = "Test conversation to test the implementation within KUtils."

    steps {
        step {
            prompt = embed {
                setTitle("Test Conversation")
                field {
                    name = "Step 1"
                    value = "To test various use cases I'd like you to tell me a user Id to start with."
                }
                setColor(Color.CYAN)
            }
            expect = UserArg
        }
        step {
            prompt = embed {
                setTitle("Test Conversation")
                field {
                    name = "Step 2"
                    value = "Alright, now tell me a random word."
                }
                setColor(Color.CYAN)
            }
            expect = WordArg
        }
    }

    onComplete {
        val user = it.responses.component1() as User
        val word = it.responses.component2() as String
        val summary = embed {
            setTitle("Summary - That's what you've told me")
            setThumbnail(user.avatarUrl)
            field {
                name = "Some user account"
                value = "The account of **${user.name}** was created on **${user.creationTime}**."
            }
            field {
                name = "Random word"
                value = "You've said **$word**."
            }
            addBlankField(true)
            field {
                name = "Test Completed"
                value = "Thanks for participating!"
            }
            setColor(Color.GREEN)
        }
        it.respond(summary)
    }
}
```

Now you can see a lot of code here, so let's try to break it down into manageable pieces. Firstly, it is worth understanding
that once you start a conversation, the bot will ask questions and gather responses for each question. Each question
(also known as a `step`) has a command argument with it. So for example, you could ask the user their age, and say 
that the expected argument is an `IntegerArg`. If they enter anything other than that, it will ask them again with 
the regular error message that `IntegerArg` supplies. 

You can see that `steps` contains many `step` invocations (as many or as little as you want) and each `step` has a 
`prompt` and an `expect` option. Prompts are given to the user via direct message in order to get them to provide 
something that matches the `expect` option, like explained before. That's all there is to building a conversation.

Next, when you the user has completed all of the steps, the `onComplete` block is triggered. Here you can get out 
all of the responses from the `responses` object, and work with them as you would the `args` object in an `execute` 
block. 

How do you trigger a conversation, though? This is kind of tricky to understand without first explaining the next 
subject, `dependency injection`. The long and short of it is that you can say that any commandset, conversation, listener or
precondition depends on a particular object or piece of data. How you do this is you add the thing you need to the 
function that you are using to create whatever it is you are creating. 

So, for this example, we want to be able to get a command to "start" the conversation we just made. We want it to 
start a conversation with however invokes the !start command. We can use the ConversationService for this. 

```kotlin
@CommandSet("sample")
fun conversationCommands(conversationService: ConversationService) {
    command("start") {
        description = "Starts a conversation with a user when invoked"
        requiresGuild = true
        execute {
            conversationService.createConversation(it.author.id, it.guild!!.id, "test-conversation")
        }
    }
}
```

You'll notice a few things: 
 - Conversations are started using the `createConversation` function. This function takes 3 things:
    - The ID of the user who you want to start the conversation with. 
    - The GuildID (legacy, will eventually be possible without providing a guildID)
    - The conversation name
 - Conversations are invoked by name. This means that the name you set on the conversation declaration is important.
  You may want to set it as a `const val` on an `object` so that you don't forget it or mistype it.


#### Creating and using a Service   

##### Overview

Services in KUtils are very useful for allowing you to pass around things you may need for building functionality. 
Say you build a `DatabaseManager`. You can imagine that this is some object which connects to your database for 
performing CRUD operations. Maybe you need it for a !warn command. That being the case, how do you allow the !warn
command to access it? Quite simple, let's take a look

```kotlin
//in any DatabaseManager.kt
@Service
class DatabaseManager {
    //...
}

//in Moderatorcommands.kt

@CommandSet("moderation")
fun createModeratorCommands(dbManager: DatabaseManager) = commands {
    command("warn") {
        //dbManager is fully accessible here or in any other command in this function
    }
}
```

As you can see, simply be decorating the class with `@Service` and then accepting an instance of a `DatabaseManager` as 
a parameter, it will be automatically passed into the right place. 

##### Services which depend on services

If you have a service that depends on another service, that's no problem. KUtils will figure out what depends on what
and invoke things the right way. This example works:

```kotlin
@Service
class A

@Service
class B(val a: A)

@Service
class C(val a: A, val b: B)
```

In this case it will figure out that A must be invoked first, then B, then C, and so on.

Do note: **Services can be passed into preconditions, other services, conversations and commands**

#### Creating and using auto-injected data objects

As a final utility, there is a nice method for getting data objects into your services. Remember our `DatabaseManager`?
Say you want to pass a `DatabaseConfig` into it, how do you do that? Simple, the `@Data` annotation. 

This will allow you manage a config (or any other data, like a messages.json). The way this works is you specify 
the `path` in the `@Data` annotation, if there is a `.json` file at the path, then it will read that file, deserialize 
it into an instance of the object that you annotated and make it available for injection. Consider the following:

```kotlin
@Data("config.json")
class BotConfiguration(val prefix: String = "!", val ownerID: String = "my-id-here")
```

As you can see, there are default values provided. If the file does not exist, the no-arg constructor will be used to 
create and write the file the first time. 

#### Add to your project with Maven
Under the dependencies tag, add

```xml
<dependency>
    <groupId>com.gitlab.aberrantfox</groupId>
    <artifactId>Kutils</artifactId>
    <version>0.9.14</version>
</dependency>
```

Under the repositories tag, add

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

#### Add to your project with Gradle
```groovy
repositories {
    //...
    maven { url 'https://jitpack.io' }
    jcenter()
    //...
}

dependencies {
    //...
    implementation "com.gitlab.aberrantfox:Kutils:0.9.16"
    //...
}
```

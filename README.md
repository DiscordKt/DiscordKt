<p align="center">
  <a href="https://discord.gg/REZVVjA">
    <img src="https://img.shields.io/discord/453208597082406912?logo=discord" alt="chat on Discord">
  </a>
</p>

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

##### Additional command options

There are some more tricks to commands that will help you achieve the kind of functionality you want. For example,
you can set it such that a command requires being invoked in a guild by setting `requiresGuild` to true on the `command`
block. 

You also have the option to add generic checks prior to all command invocations. If you wanted to build your own permission
system, you would need this. 

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

#### Add to your project with Maven
Under the dependencies tag, add

```xml
<dependency>
    <groupId>com.gitlab.aberrantfox</groupId>
    <artifactId>Kutils</artifactId>
    <version>0.10.1</version>
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
    implementation "com.gitlab.aberrantfox:Kutils:0.10.1"
    //...
}
```

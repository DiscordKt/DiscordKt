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


#### Add to your project with Maven
Under the dependencies tag, add

```xml
<dependency>
    <groupId>com.gitlab.aberrantfox</groupId>
    <artifactId>Kutils</artifactId>
    <version>0.9.10</version>
</dependency>
```

Under the repositories tag, add

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

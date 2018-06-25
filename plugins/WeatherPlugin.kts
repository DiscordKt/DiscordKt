/*
 * Variables available:
 *  - container - a commands container
 *  - kutils - a hook to the main object
 *
 * Please note that if you want to import additional stuff elsewhere, you do need an import statement :)
 */


//example using container to add a new command set
container.join(commands {
    command("weather") {
        description = "A command to tell you the weather!"
        execute {
            it.respond("It's rainy, take a coat!")
        }
    }

    command("try-me") {
        description = "Try to run this command!"
        execute {
            it.respond("What did you expect, a medal?")
        }
    }
})

//example using kutils to get the jda object
println(kutils.jda.selfUser)
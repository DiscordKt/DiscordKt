import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.commands

val container = bindings["container"] as CommandsContainer

container.join(commands {
    command("weather") {
        description = "A command to tell you the weather!"
        execute {
            it.respond("It's rainy, take a coat!")
        }
    }
})
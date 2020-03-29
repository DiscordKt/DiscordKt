package me.aberrantfox.kjdautils.examples.implementations

import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.internal.di.PersistenceService

//Data objects are automatically loaded from the designated path on startup.
//If the file does not exist at the designated path, it is created using the default arguments.
@Data("config.json")
data class ConfigurationObject(var prefix: String = "!")

@CommandSet("Data Demo")
fun dataCommands(config: ConfigurationObject, persistenceService: PersistenceService) = commands {
    command("DataSee") {
        description = "This command lets you view a Data object's contents."
        execute {
            it.respond(config.prefix)
        }
    }
    command("DataSave") {
        description = "This command lets you modify a Data object's contents."
        execute {
            config.prefix = "different"
            persistenceService.save(config)
        }
    }
}
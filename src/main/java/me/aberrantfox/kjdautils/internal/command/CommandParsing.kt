package me.aberrantfox.kjdautils.internal.command

import kotlinx.coroutines.experimental.runBlocking
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.api.configuration.Configuration
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandArgument
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.internal.listeners.CommandListener
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

const val seperatorCharacter = "|"

annotation class CommandSet

data class CommandStruct(val commandName: String, val commandArgs: List<String> = listOf())

internal fun produceContainer(pack: String): CommandsContainer {
    val cmds = Reflections(pack, MethodAnnotationsScanner()).getMethodsAnnotatedWith(CommandSet::class.java)

    val container = cmds.map { it.invoke(null) }
        .map { it as CommandsContainer }
        .reduce { a, b -> a.join(b) }

    val lowMap = HashMap<String, Command>()

    container.commands.keys.forEach {
        lowMap.put(it.toLowerCase(), container.commands[it]!!)
    }

    container.commands = lowMap

    return container
}

internal fun convertAndQueue(actual: List<String>, expected: List<CommandArgument>,
                    instance: CommandListener, event: CommandEvent,
                    invokedInGuild: Boolean, command: Command,
                    config: Configuration) {

    val expectedTypes = expected.map { it.type }

    if (expectedTypes.contains(ArgumentType.Manual)) {
        instance.executeEvent(command, event, invokedInGuild)
        return
    }

    val convertedArgs = convertMainArgs(actual, expected)

    if (convertedArgs == null) {
        event.respond("Incorrect arguments passed to command, try viewing the help documentation via: ${config.serverInformation.prefix}help <commandName>")
        return
    }

    val filledArgs = convertOptionalArgs(convertedArgs, expected, event)

    event.args = filledArgs

    if (expectedTypes.contains(ArgumentType.User)) {
        dispatchRequestRequiredEvent(expectedTypes, filledArgs, event, command, instance, invokedInGuild)
    } else {
        instance.executeEvent(command, event, invokedInGuild)
    }
}

private fun dispatchRequestRequiredEvent(expected: List<ArgumentType>, standard: List<Any>, event: CommandEvent, command: Command,
                                         instance: CommandListener, invokedInGuild: Boolean) {
    val zip = standard.zip(expected)

    runBlocking {
        val fullyParsed = ArrayList<Any>()

        zip.forEach {
            if (it.second == ArgumentType.User) {
                try{
                    val parsedUser = event.jda.retrieveUserById((it.first as String).trimToID()).complete()
                    if(parsedUser == null) {
                        event.safeRespond("Error, cannot find user by ID: ${it.first}")
                        return@runBlocking
                    }

                    fullyParsed.add(parsedUser)
                } catch (e: Exception) {
                    event.safeRespond("Error, cannot find user: ${it.first}")
                    return@runBlocking
                }
            } else {
                fullyParsed.add(it.first)
            }
        }
        event.args = fullyParsed
        instance.executeEvent(command, event, invokedInGuild)
    }
}

fun getCommandStruct(message: String, config: Configuration): CommandStruct {
    var trimmedMessage = message.substring(config.serverInformation.prefix.length)

    if (trimmedMessage.startsWith(config.serverInformation.prefix)) trimmedMessage = trimmedMessage.substring(config.serverInformation.prefix.length)

    if (!(message.contains(" "))) {
        return CommandStruct(trimmedMessage.toLowerCase())
    }

    val commandName = trimmedMessage.substring(0, trimmedMessage.indexOf(" ")).toLowerCase()
    val commandArgs = trimmedMessage.substring(trimmedMessage.indexOf(" ") + 1).split(" ")

    return CommandStruct(commandName, commandArgs)
}
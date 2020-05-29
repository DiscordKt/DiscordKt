package me.jakejmattson.kutils.api.dsl.command

import me.jakejmattson.kutils.api.annotations.CommandSet
import me.jakejmattson.kutils.api.extensions.stdlib.pluralize
import me.jakejmattson.kutils.api.services.DIService
import me.jakejmattson.kutils.internal.utils.InternalLogger
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

@DslMarker
annotation class CommandTagMarker

fun commands(construct: CommandsContainer.() -> Unit): CommandsContainer {
    val commands = CommandsContainer()
    commands.construct()
    return commands
}

@CommandTagMarker
data class CommandsContainer(var commands: ArrayList<Command> = arrayListOf()) {
    fun command(vararg names: String, body: Command.() -> Unit): Command {
        val command = Command(names.toList())
        command.body()
        commands.add(command)
        return command
    }

    operator fun plus(container: CommandsContainer) = this.apply { commands.addAll(container.commands) }
    operator fun get(name: String) = this.commands.firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
}

fun produceContainer(path: String, diService: DIService): CommandsContainer {
    val commandSets = Reflections(path, MethodAnnotationsScanner())
        .getMethodsAnnotatedWith(CommandSet::class.java)
        .map { it to (it.annotations.first { it is CommandSet } as CommandSet).category }

    if (commandSets.isEmpty()) {
        InternalLogger.startup("0 CommandSets -> 0 Commands")
        return CommandsContainer()
    }

    val container = commandSets
        .map { (method, cmdSetCategory) ->
            diService.invokeReturningMethod<CommandsContainer>(method) to cmdSetCategory
        }
        .map { (container, cmdSetCategory) ->
            container.also {
                it.commands
                    .filter { it.category == "" }
                    .forEach { it.category = cmdSetCategory }
            }
        }
        .reduce { a, b -> a + b }

    InternalLogger.startup(commandSets.size.pluralize("CommandSet") + " -> " + container.commands.size.pluralize("Command"))

    return container
}
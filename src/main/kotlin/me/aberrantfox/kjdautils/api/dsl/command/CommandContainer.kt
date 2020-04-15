package me.aberrantfox.kjdautils.api.dsl.command

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.extensions.stdlib.pluralize
import me.aberrantfox.kjdautils.internal.logging.InternalLogger
import me.aberrantfox.kjdautils.internal.services.DIService
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
    operator fun invoke(args: CommandsContainer.() -> Unit) {}

    fun command(vararg names: String, construct: Command.() -> Unit = {}): Command? {
        val command = Command(names.toMutableList())
        command.construct()
        this.commands.add(command)
        return command
    }

    fun join(vararg cmds: CommandsContainer): CommandsContainer {
        cmds.forEach {
            this.commands.addAll(it.commands)
        }

        return this
    }

    operator fun get(name: String) = this.commands.firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
}

fun produceContainer(path: String, diService: DIService): CommandsContainer {
    val commandSets = Reflections(path, MethodAnnotationsScanner())
        .getMethodsAnnotatedWith(CommandSet::class.java)
        .map { it to (it.annotations.first { it is CommandSet } as CommandSet).category }

    if (commandSets.isEmpty()) {
        InternalLogger.info("No command methods detected.")
        return CommandsContainer()
    }

    val container = commandSets
        .map { (method, cmdSetCategory) ->
            (diService.invokeReturningMethod(method) as CommandsContainer) to cmdSetCategory
        }
        .map { (container, cmdSetCategory) ->
            container.also {
                it.commands
                    .filter { it.category == "" }
                    .forEach { it.category = cmdSetCategory }
            }
        }
        .reduce { a, b -> a.join(b) }

    InternalLogger.startup(commandSets.size.pluralize("CommandSet") + " -> " + container.commands.size.pluralize("command"))

    return container
}
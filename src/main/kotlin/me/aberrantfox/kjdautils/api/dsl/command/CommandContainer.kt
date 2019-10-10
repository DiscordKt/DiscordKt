package me.aberrantfox.kjdautils.api.dsl.command

import me.aberrantfox.kjdautils.internal.di.DIService
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner

@DslMarker
annotation class CommandTagMarker

annotation class CommandSet(val category: String = "uncategorized")

fun commands(construct: CommandsContainer.() -> Unit): CommandsContainer {
    val commands = CommandsContainer()
    commands.construct()
    return commands
}

@CommandTagMarker
data class CommandsContainer(var commands: HashMap<String, Command> = HashMap()) {
    operator fun invoke(args: CommandsContainer.() -> Unit) {}

    fun listCommands() = this.commands.keys.toList()

    fun command(name: String, construct: Command.() -> Unit = {}): Command? {
        val command = Command(name)
        command.construct()
        this.commands[name] = command
        return command
    }

    fun join(vararg cmds: CommandsContainer): CommandsContainer {
        cmds.forEach {
            this.commands.putAll(it.commands)
        }

        return this
    }

    fun has(name: String) = this.commands.containsKey(name)

    operator fun get(name: String) = this.commands.values.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }
}

fun produceContainer(path: String, diService: DIService): CommandsContainer {
    val cmdMethods = Reflections(path, MethodAnnotationsScanner())
        .getMethodsAnnotatedWith(CommandSet::class.java)
        .map { it to (it.annotations.first { it is CommandSet } as CommandSet).category }

    if(cmdMethods.isEmpty()) {
        println("KUtils: No command methods detected.")
        return CommandsContainer()
    } else {
        println("KUtils: ${cmdMethods.size} command methods detected.")
    }

    val container = cmdMethods
        .map { (method, cmdSetCategory) ->
            (diService.invokeReturningMethod(method) as CommandsContainer) to cmdSetCategory
        }
        .map { (container, cmdSetCategory) ->
            container.also {
                it.commands.values
                    .filter { it.category == "" }
                    .forEach { it.category = cmdSetCategory }
            }
        }
        .reduce { a, b -> a.join(b) }

    val lowMap = container.commands.mapKeys { it.key.toLowerCase() } as HashMap<String, Command>
    container.commands = lowMap

    return container
}
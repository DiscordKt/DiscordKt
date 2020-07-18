package me.jakejmattson.kutils.api.dsl.command

import me.jakejmattson.kutils.api.annotations.BuilderDSL

/**
 * Create a container where multiple commands can be created.
 */
@BuilderDSL
fun commands(construct: CommandsContainer.() -> Unit): CommandsContainer {
    val commands = CommandsContainer()
    commands.construct()
    return commands
}

/**
 * A container for a collection of commands and convenience methods.
 *
* @param commands A list of all currently registered commands.
 */
data class CommandsContainer(val commands: MutableList<Command> = mutableListOf()) {
    /**
     * A block to be used for creating a new command.
     */
    fun command(vararg names: String, body: Command.() -> Unit): Command {
        val command = Command(names.toList())
        command.body()
        commands.add(command)
        return command
    }

    /** @suppress operator */
    operator fun plus(container: CommandsContainer) = apply { commands.addAll(container.commands) }

    /** @suppress operator **/
    operator fun get(name: String) = commands.firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
}
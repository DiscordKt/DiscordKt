package me.jakejmattson.kutils.api.dsl.command

/**
 * Create a container where multiple commands can be created.
 */
fun commands(construct: CommandsContainer.() -> Unit): CommandsContainer {
    val commands = CommandsContainer()
    commands.construct()
    return commands
}

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

    operator fun plus(container: CommandsContainer) = apply { commands.addAll(container.commands) }
    operator fun get(name: String) = commands.firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
}
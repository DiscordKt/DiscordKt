@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL

/**
 * Create a block for registering commands.
 *
 * @param category The category these commands will be under.
 * @param construct The builder function.
 */
@BuilderDSL
fun commands(category: String, construct: CommandSetBuilder.() -> Unit) = CommandSet(category, construct)

/**
 * @suppress Used in DSL
 */
data class CommandSetBuilder(val discord: Discord, val category: String) {
    private val commands = mutableListOf<Command>()

    /**
     * Create a new command in this list.
     */
    fun command(vararg names: String, body: Command.() -> Unit) {
        val command = Command(names.toList(), category = category)
        command.body()
        commands.add(command)
    }

    internal fun registerCommands() {
        discord.commands.addAll(commands)
    }
}

/**
 * This is not for you...
 */
data class CommandSet(private val category: String, private val collector: CommandSetBuilder.() -> Unit) {
    internal fun registerCommands(discord: Discord) {
        val commandSetBuilder = CommandSetBuilder(discord, category)
        collector.invoke(commandSetBuilder)
        commandSetBuilder.registerCommands()
    }
}
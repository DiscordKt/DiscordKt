@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.annotations.InnerDSL
import me.jakejmattson.discordkt.internal.utils.BuilderRegister

/**
 * Create a block for registering commands.
 *
 * @param category The category these commands will be under.
 * @param construct The builder function.
 */
@BuilderDSL
fun commands(category: String, requiredPermissionLevel: Enum<*>? = null, construct: CommandSetBuilder.() -> Unit) = CommandSet(category, requiredPermissionLevel, construct)

/**
 * DSL used to build a set of commands.
 *
 * @param discord The discord instance.
 * @param category The category these commands will be under.
 */
data class CommandSetBuilder(val discord: Discord, val category: String, private val requiredPermission: Enum<*>) {
    private val commands = mutableListOf<Command>()

    /**
     * Create a global command.
     */
    @InnerDSL
    fun command(vararg names: String, body: GlobalCommand.() -> Unit) {
        val command = GlobalCommand(names.toList(), "", requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a slash command.
     */
    @InnerDSL
    fun slash(name: String, body: GlobalSlashCommand.() -> Unit) {
        val command = GlobalSlashCommand(name, "", requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a guild command.
     */
    @InnerDSL
    fun guildCommand(vararg names: String, body: GuildCommand.() -> Unit) {
        val command = GuildCommand(names.toList(), "", requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a dm command.
     */
    @InnerDSL
    fun dmCommand(vararg names: String, body: DmCommand.() -> Unit) {
        val command = DmCommand(names.toList(), "", requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

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
class CommandSet(private val category: String, private val requiredPermissionLevel: Enum<*>?, private val collector: CommandSetBuilder.() -> Unit) : BuilderRegister {
    /** @suppress */
    override fun register(discord: Discord) {
        val permissionLevel = requiredPermissionLevel ?: discord.configuration.defaultRequiredPermission
        val commandSetBuilder = CommandSetBuilder(discord, category, permissionLevel)
        collector.invoke(commandSetBuilder)
        commandSetBuilder.registerCommands()
    }
}
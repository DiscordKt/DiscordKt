@file:Suppress("unused")

package me.jakejmattson.discordkt.api.commands

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
public fun commands(category: String, requiredPermissionLevel: Enum<*>? = null, construct: CommandSetBuilder.() -> Unit): CommandSet = CommandSet(category, requiredPermissionLevel, construct)

/**
 * DSL used to build a set of commands.
 *
 * @param discord The discord instance.
 * @param category The category these commands will be under.
 */
public data class CommandSetBuilder(val discord: Discord, val category: String, private val requiredPermission: Enum<*>) {
    private val commands = mutableListOf<Command>()

    /**
     * Create a guild command.
     */
    @InnerDSL
    public fun command(vararg names: String, body: GuildCommand.() -> Unit) {
        val command = GuildCommand(names.toList(), requiredPermission = requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a guild command.
     */
    @InnerDSL
    @Deprecated("Guild commands are now the default 'command' behavior", level = DeprecationLevel.WARNING)
    public fun guildCommand(vararg names: String, body: GuildCommand.() -> Unit) {
        val command = GuildCommand(names.toList(), requiredPermission = requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a dm command.
     */
    @InnerDSL
    public fun dmCommand(vararg names: String, body: DmCommand.() -> Unit) {
        val command = DmCommand(names.toList(), requiredPermission = requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a global command.
     */
    @InnerDSL
    public fun globalCommand(vararg names: String, body: GlobalCommand.() -> Unit) {
        val command = GlobalCommand(names.toList(), requiredPermission = requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a slash command.
     */
    @InnerDSL
    public fun slash(name: String, appName: String = name, body: GuildSlashCommand.() -> Unit) {
        val command = GuildSlashCommand(name, appName, requiredPermission = requiredPermission).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a slash command.
     */
    @InnerDSL
    public fun globalSlash(name: String, appName: String = name, body: GlobalSlashCommand.() -> Unit) {
        val command = GlobalSlashCommand(name, appName, requiredPermission = requiredPermission).apply {
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
public class CommandSet(private val category: String, private val requiredPermissionLevel: Enum<*>?, private val collector: CommandSetBuilder.() -> Unit) : BuilderRegister {
    /** @suppress */
    override fun register(discord: Discord) {
        val permissionLevel = requiredPermissionLevel ?: discord.permissions.commandDefault
        val commandSetBuilder = CommandSetBuilder(discord, category, permissionLevel)
        collector.invoke(commandSetBuilder)
        commandSetBuilder.registerCommands()
    }
}
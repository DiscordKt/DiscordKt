@file:Suppress("unused")

package me.jakejmattson.discordkt.commands

import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.arguments.UserArg
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
public fun commands(category: String, requiredPermissionLevel: Permissions? = null, construct: CommandSetBuilder.() -> Unit): CommandSet = CommandSet(category, requiredPermissionLevel, construct)

/**
 * DSL used to build a set of commands.
 *
 * @param discord The discord instance.
 * @param category The category these commands will be under.
 */
public data class CommandSetBuilder(val discord: Discord, val category: String, private val requiredPermissions: Permissions) {
    private val commands = mutableListOf<Command>()

    /**
     * Create a guild command.
     */
    @InnerDSL
    @Deprecated("Generic 'command' functions will be replaced with explicit names.", ReplaceWith("text(*names) { body() }"))
    public fun command(vararg names: String, body: (GuildTextCommand.() -> Unit)) {
        val command = GuildTextCommand(names.toList(), category = category, requiredPermissions = requiredPermissions)
        command.body()
        commands.add(command)
    }

    /**
     * Create a guild text command.
     *
     * @param names The names of this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun text(vararg names: String, action: GuildTextCommand.() -> Unit) {
        val command = GuildTextCommand(names.toList(), category = category, requiredPermissions = requiredPermissions)
        command.action()
        commands.add(command)
    }

    /**
     * Create a dm command.
     */
    @InnerDSL
    @Deprecated("Generic 'command' functions will be replaced with explicit names.", ReplaceWith("dmText(*names) { body() }"))
    public fun dmCommand(vararg names: String, body: DmTextCommand.() -> Unit) {
        val command = DmTextCommand(names.toList(), category = category, requiredPermissions = requiredPermissions)
        command.body()
        commands.add(command)
    }

    /**
     * Create a dm text command.
     *
     * @param names The names of this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun dmText(vararg names: String, action: DmTextCommand.() -> Unit) {
        val command = DmTextCommand(names.toList(), category = category, requiredPermissions = requiredPermissions)
        command.action()
        commands.add(command)
    }

    /**
     * Create a global command.
     */
    @InnerDSL
    @Deprecated("Generic 'command' functions will be replaced with explicit names.", ReplaceWith("globalText(*names) { body() }"))
    public fun globalCommand(vararg names: String, body: GlobalTextCommand.() -> Unit) {
        val command = GlobalTextCommand(names.toList(), category = category, requiredPermissions = requiredPermissions)
        command.body()
        commands.add(command)
    }

    /**
     * Create a global text command.
     *
     * @param names The names of this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun globalText(vararg names: String, action: GlobalTextCommand.() -> Unit) {
        val command = GlobalTextCommand(names.toList(), category = category, requiredPermissions = requiredPermissions)
        command.action()
        commands.add(command)
    }

    /**
     * Create a guild slash command.
     *
     * @param name The name of this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun slash(name: String, action: GuildSlashCommand.() -> Unit) {
        val command = GuildSlashCommand(name, category = category, requiredPermissions = requiredPermissions)
        command.action()
        commands.add(command)
    }

    /**
     * Create a global slash command.
     *
     * @param name The name of this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun globalSlash(name: String, action: GlobalSlashCommand.() -> Unit) {
        val command = GlobalSlashCommand(name, category = category, requiredPermissions = requiredPermissions)
        command.action()
        commands.add(command)
    }

    /**
     * Create a message context command.
     *
     * @param displayText The text to display in the context menu.
     * @param slashName The name to register as a slash command.
     * @param description The description for the slash command.
     * @param action The command action.
     */
    @InnerDSL
    public fun user(displayText: String, slashName: String, description: String, action: suspend ContextEvent<User>.() -> Unit) {
        val command = ContextCommand(slashName, displayText, description = description, category = category, requiredPermissions = requiredPermissions).apply {
            execute(UserArg) {
                this.toContextual(args.first).action()
            }
        }

        commands.add(command)
    }

    /**
     * Create a message context command.
     *
     * @param displayText The text to display in the context menu.
     * @param slashName The name to register as a slash command.
     * @param description The description for the slash command.
     * @param action The command action.
     */
    @InnerDSL
    public fun message(displayText: String, slashName: String, description: String, action: suspend ContextEvent<Message>.() -> Unit) {
        val command = ContextCommand(slashName, displayText, description = description, category = category, requiredPermissions = requiredPermissions).apply {
            execute(MessageArg) {
                this.toContextual(args.first).action()
            }
        }

        commands.add(command)
    }

    internal fun registerCommands() {
        discord.commands.addAll(commands)
    }
}

/**
 * This is not for you...
 */
public class CommandSet(private val category: String, private val requiredPermissionLevel: Permissions?, private val collector: CommandSetBuilder.() -> Unit) : BuilderRegister {
    /** @suppress */
    override fun register(discord: Discord) {
        val permissionLevel = requiredPermissionLevel ?: discord.configuration.defaultPermissions
        val commandSetBuilder = CommandSetBuilder(discord, category, permissionLevel)
        collector.invoke(commandSetBuilder)
        commandSetBuilder.registerCommands()
    }
}
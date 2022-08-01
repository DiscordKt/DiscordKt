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
        val command = GuildTextCommand(names.toList(), requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a guild text command.
     */
    @InnerDSL
    public fun text(vararg names: String, body: GuildTextCommand.() -> Unit) {
        val command = GuildTextCommand(names.toList(), requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a dm command.
     */
    @InnerDSL
    @Deprecated("Generic 'command' functions will be replaced with explicit names.", ReplaceWith("dmText(*names) { body() }"))
    public fun dmCommand(vararg names: String, body: DmTextCommand.() -> Unit) {
        val command = DmTextCommand(names.toList(), requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a dm text command.
     */
    @InnerDSL
    public fun dmText(vararg names: String, body: DmTextCommand.() -> Unit) {
        val command = DmTextCommand(names.toList(), requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a global command.
     */
    @InnerDSL
    @Deprecated("Generic 'command' functions will be replaced with explicit names.", ReplaceWith("globalText(*names) { body() }"))
    public fun globalCommand(vararg names: String, body: GlobalTextCommand.() -> Unit) {
        val command = GlobalTextCommand(names.toList(), requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a global text command.
     */
    @InnerDSL
    public fun globalText(vararg names: String, body: GlobalTextCommand.() -> Unit) {
        val command = GlobalTextCommand(names.toList(), requiredPermissions = requiredPermissions).apply {
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
        val command = GuildSlashCommand(name, appName, requiredPermissions = requiredPermissions).apply {
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
        val command = GlobalSlashCommand(name, appName, requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category
        }

        command.body()
        commands.add(command)
    }

    /**
     * Create a user context command.
     */
    @InnerDSL
    public fun user(displayName: String, slashName: String, description: String, body: suspend ContextEvent<User>.() -> Unit) {
        val command = GuildSlashCommand(slashName, displayName, description = description, requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category

            execute(UserArg) {
                this.toContextual(args.first).body()
            }
        }

        commands.add(command)
    }

    /**
     * Create a message context command.
     */
    @InnerDSL
    public fun message(displayName: String, slashName: String, description: String, body: suspend ContextEvent<Message>.() -> Unit) {
        val command = GuildSlashCommand(slashName, displayName, description = description, requiredPermissions = requiredPermissions).apply {
            this.category = this@CommandSetBuilder.category

            execute(MessageArg) {
                this.toContextual(args.first).body()
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
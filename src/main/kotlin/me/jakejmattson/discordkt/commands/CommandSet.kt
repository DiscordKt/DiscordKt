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
     * Create a guild slash command.
     *
     * @param name The name of this command.
     * @param description The description of this command.
     * @param requiredPermissions The [Permissions] required to run this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun slash(name: String, description: String = "", requiredPermissions: Permissions = this.requiredPermissions, action: GuildSlashCommand.() -> Unit) {
        val command = GuildSlashCommand(name, description, category, requiredPermissions)
        command.action()
        commands.add(command)
    }

    /**
     * Create a global slash command.
     *
     * @param name The name of this command.
     * @param description The description of this command.
     * @param requiredPermissions The [Permissions] required to run this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun globalSlash(name: String, description: String = "", requiredPermissions: Permissions = this.requiredPermissions, action: GlobalSlashCommand.() -> Unit) {
        val command = GlobalSlashCommand(name, description, category, requiredPermissions)
        command.action()
        commands.add(command)
    }

    /**
     * Create a message context command.
     *
     * @param displayText The text to display in the context menu.
     * @param slashName The name to register as a slash command.
     * @param description The description for the slash command.
     * @param requiredPermissions The [Permissions] required to run this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun user(displayText: String,
                    slashName: String,
                    description: String,
                    requiredPermissions: Permissions = this.requiredPermissions,
                    action: suspend ContextEvent<User>.() -> Unit) {
        val command = ContextCommand(slashName, displayText, description, category, requiredPermissions).apply {
            execute(UserArg) {
                this.toContextual(args.first).also { it.args = args }.action()
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
     * @param requiredPermissions The [Permissions] required to run this command.
     * @param action The command action.
     */
    @InnerDSL
    public fun message(displayText: String,
                       slashName: String,
                       description: String,
                       requiredPermissions: Permissions = this.requiredPermissions,
                       action: suspend ContextEvent<Message>.() -> Unit) {
        val command = ContextCommand(slashName, displayText, description, category, requiredPermissions).apply {
            execute(MessageArg) {
                this.toContextual(args.first).also { it.args = args }.action()
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
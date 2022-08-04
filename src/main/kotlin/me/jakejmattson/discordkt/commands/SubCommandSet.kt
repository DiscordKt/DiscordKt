package me.jakejmattson.discordkt.commands

import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.annotations.InnerDSL
import me.jakejmattson.discordkt.internal.utils.BuilderRegister

/**
 * Create a block for registering commands.
 *
 * @param name The category these commands will be under.
 * @param construct The builder function.
 */
@BuilderDSL
public fun subcommand(name: String, requiredPermissionLevel: Permissions? = null, construct: SubCommandSetBuilder.() -> Unit): SubCommandSet = SubCommandSet(name, requiredPermissionLevel, construct)

/**
 * DSL used to build a set of commands.
 *
 * @param discord The discord instance.
 * @param category The category these commands will be under.
 */
public data class SubCommandSetBuilder(val discord: Discord, val category: String, private val requiredPermissions: Permissions) {
    internal val commands = mutableListOf<GuildSlashCommand>()

    /**
     * Create a slash command.
     */
    @InnerDSL
    public fun sub(name: String, body: GuildSlashCommand.() -> Unit) {
        val command = GuildSlashCommand(name, category = category, requiredPermissions = requiredPermissions)
        command.body()
        commands.add(command)
    }
}

/**
 * This is not for you...
 */
public class SubCommandSet(internal val name: String, internal val requiredPermissionLevel: Permissions?, private val collector: SubCommandSetBuilder.() -> Unit) : BuilderRegister {
    internal val commands: MutableList<GuildSlashCommand> = mutableListOf()

    /** @suppress */
    override fun register(discord: Discord) {
        val permissionLevel = requiredPermissionLevel ?: discord.configuration.defaultPermissions
        val subCommandSetBuilder = SubCommandSetBuilder(discord, name, permissionLevel)
        collector.invoke(subCommandSetBuilder)
        commands.addAll(subCommandSetBuilder.commands)
        discord.subcommands.add(this)
    }
}
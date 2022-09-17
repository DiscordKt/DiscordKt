@file:Suppress("unused")

package me.jakejmattson.discordkt.commands

import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import me.jakejmattson.discordkt.*
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.internal.annotations.NestedDSL

/**
 * The bundle of information to be executed when a command is invoked.
 *
 * @param arguments The [Argument]s accepted by this execution.
 * @param action The code to be run when this execution is fired.
 */
public data class Execution<T : CommandEvent<*>>(val arguments: List<Argument<*, *>>, val action: suspend T.() -> Unit) {
    /**
     * Mocks a method signature using [Argument] names, ex: (a, b, c)
     */
    val signature: String
        get() = "(${arguments.joinToString { it.name }})"

    /**
     * Each [Argument] separated by a space ex: a b c
     */
    val structure: String
        get() = arguments.joinToString(" ") {
            val type = it.name
            if (it.isOptional()) "[$type]" else type
        }

    /**
     * Run the command logic.
     */
    public suspend fun execute(event: T): Unit = action.invoke(event)
}

/**
 *
 * @property name The name of this command.
 * @property names All names (aliases) of this command.
 * @property description A brief description of the command - used in documentation.
 * @property requiredPermissions The permission level required to use this command.
 * @property category The category that this command belongs to - set automatically by CommandSet.
 * @property execution The [Execution] that this command can be run with.
 */
public sealed interface Command {
    public val name: String
    public val description: String
    public val category: String
    public val requiredPermissions: Permissions
    public var execution: Execution<CommandEvent<*>>

    public val names: List<String>
        get() = listOf(name)

    /**
     * Whether this command has permission to run with the given event.
     *
     * @param discord The event context that will attempt to run the command.
     */
    public suspend fun hasPermissionToRun(discord: Discord, author: User, guild: Guild?): Boolean =
        if (guild != null)
            author.asMember(guild.id).getPermissions().contains(requiredPermissions)
        else
            false //TODO Handle global message commands

    /**
     * Add an [Execution] to this [Command].
     * Called automatically by each execute block.
     * You should not need to call this manually.
     */
    public fun <T : CommandEvent<*>> addExecution(argTypes: List<Argument<*, *>>, execute: suspend T.() -> Unit) {
        execution = Execution(argTypes, execute) as Execution<CommandEvent<*>>
    }
}

/**
 * A slash command that can be executed anywhere.
 *
 * @property name The name of the slash command.
 */
public class GlobalSlashCommand(override val name: String,
                                override val description: String,
                                override val category: String,
                                override val requiredPermissions: Permissions,
                                override var execution: Execution<CommandEvent<*>> = Execution(listOf()) {}) : Command {
    /** @suppress */
    @NestedDSL
    public fun execute(execute: suspend SlashCommandEvent<NoArgs>.() -> Unit): Unit = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    public fun <A> execute(a: Argument<*, A>, execute: suspend SlashCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B> execute(a: Argument<*, A>, b: Argument<*, B>, execute: suspend SlashCommandEvent<Args2<A, B>>.() -> Unit): Unit = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, execute: suspend SlashCommandEvent<Args3<A, B, C>>.() -> Unit): Unit = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, execute: suspend SlashCommandEvent<Args4<A, B, C, D>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D, E> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, e: Argument<*, E>, execute: suspend SlashCommandEvent<Args5<A, B, C, D, E>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A slash command that can be executed in a guild.
 *
 * @property name The name of the slash command.
 */
public open class GuildSlashCommand(override val name: String,
                                    override val description: String,
                                    override val category: String,
                                    override val requiredPermissions: Permissions,
                                    override var execution: Execution<CommandEvent<*>> = Execution(listOf()) {}) : Command {
    /** @suppress */
    @NestedDSL
    public fun execute(execute: suspend GuildSlashCommandEvent<NoArgs>.() -> Unit): Unit = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    public open fun <A> execute(a: Argument<*, A>, execute: suspend GuildSlashCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B> execute(a: Argument<*, A>, b: Argument<*, B>, execute: suspend GuildSlashCommandEvent<Args2<A, B>>.() -> Unit): Unit = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, execute: suspend GuildSlashCommandEvent<Args3<A, B, C>>.() -> Unit): Unit = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, execute: suspend GuildSlashCommandEvent<Args4<A, B, C, D>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    public fun <A, B, C, D, E> execute(a: Argument<*, A>, b: Argument<*, B>, c: Argument<*, C>, d: Argument<*, D>, e: Argument<*, E>, execute: suspend GuildSlashCommandEvent<Args5<A, B, C, D, E>>.() -> Unit): Unit = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A command that can be executed via the context menu.
 *
 * @property displayText The text shown in the context menu.
 */
public class ContextCommand<A>(override val name: String,
                               public val displayText: String,
                               override val description: String,
                               override val category: String,
                               override val requiredPermissions: Permissions,
                               public val argument: Argument<*, A>,
                               public val execute: suspend GuildSlashCommandEvent<Args1<A>>.() -> Unit,
                               override var execution: Execution<CommandEvent<*>> = Execution(listOf(argument), execute) as Execution<CommandEvent<*>>) : GuildSlashCommand(name, description, category, requiredPermissions, execution) {
    /** @suppress */
    @NestedDSL
    public override fun <A> execute(a: Argument<*, A>, execute: suspend GuildSlashCommandEvent<Args1<A>>.() -> Unit): Unit = addExecution(listOf(a), execute)
}

/**
 * Find a command by its name (case-insensitive).
 */
public fun <T : Command> List<T>.findByName(name: String): T? = find { it.name.equals(name, true) }
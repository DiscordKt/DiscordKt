@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.locale.inject
import me.jakejmattson.discordkt.internal.annotations.NestedDSL
import me.jakejmattson.discordkt.internal.command.ParseResult
import me.jakejmattson.discordkt.internal.command.convertArguments

/**
 * The bundle of information to be executed when a command is invoked.
 *
 * @param arguments The ArgumentTypes accepted by this execution.
 * @param action The code to be run when this execution is fired.
 *
 * @property signature Mocks a method signature using ArgumentType names, i.e. (a, b, c)
 */
data class Execution<T : CommandEvent<*>>(val arguments: List<ArgumentType<*>>, val action: suspend T.() -> Unit) {
    val signature
        get() = "(${arguments.joinToString { it.name }})"

    /**
     * Run the command logic.
     */
    suspend fun execute(event: T) = action.invoke(event)
}

/**
 * @property names The name(s) this command can be executed by (case insensitive).
 * @property description A brief description of the command - used in documentation.
 * @property category The category that this command belongs to - set automatically by CommandSet.
 * @property executions The list of [Execution] that this command can be run with.
 */
sealed class Command(open val names: List<String>, open var description: String) {
    var category: String = ""
    val executions: MutableList<Execution<*>> = mutableListOf()

    /**
     * Whether or not the command can parse the given arguments into a container.
     *
     * @param args The raw string arguments to be provided to the command.
     *
     * @return The result of the parsing operation.
     */
    suspend fun canParse(event: CommandEvent<*>, execution: Execution<*>, args: List<String>) = convertArguments(event, execution.arguments, args) is ParseResult.Success

    /**
     * Invoke this command with the given args.
     */
    fun invoke(event: CommandEvent<TypeContainer>, args: List<String>) {
        GlobalScope.launch {
            val success = executions.map { it to convertArguments(event, it.arguments, args) }
                .firstOrNull { it.second is ParseResult.Success }

            if (success == null) {
                event.respond(event.discord.locale.badArgs.inject(event.rawInputs.commandName))
                return@launch
            }

            val (execution, result) = success

            event.args = (result as ParseResult.Success).argumentContainer
            (execution as Execution<CommandEvent<*>>).execute(event)
        }
    }

    protected fun <T : CommandEvent<*>> addExecution(argTypes: List<ArgumentType<*>>, execute: suspend T.() -> Unit) {
        executions.add(Execution(argTypes, execute))
    }
}

/**
 * A command that can be executed from anywhere.
 */
open class GlobalCommand(override val names: List<String>, override var description: String) : Command(names, description) {
    /** @suppress */
    @NestedDSL
    fun execute(execute: suspend CommandEvent<NoArgs>.() -> Unit) = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    fun <A> execute(a: ArgumentType<A>, execute: suspend CommandEvent<Args1<A>>.() -> Unit) = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend CommandEvent<Args2<A, B>>.() -> Unit) = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend CommandEvent<Args3<A, B, C>>.() -> Unit) = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend CommandEvent<Args4<A, B, C, D>>.() -> Unit) = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend CommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A command wrapper for a discord slash command.
 *
 * @property name The name of the slash command.
 */
class SlashCommand(val name: String, override var description: String) : GlobalCommand(listOf(name), description)

/**
 * A command that can only be executed in a guild.
 */
class GuildCommand(override val names: List<String>, override var description: String) : Command(names, description) {
    /** @suppress */
    @NestedDSL
    fun execute(execute: suspend GuildCommandEvent<NoArgs>.() -> Unit) = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    fun <A> execute(a: ArgumentType<A>, execute: suspend GuildCommandEvent<Args1<A>>.() -> Unit) = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend GuildCommandEvent<Args2<A, B>>.() -> Unit) = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend GuildCommandEvent<Args3<A, B, C>>.() -> Unit) = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend GuildCommandEvent<Args4<A, B, C, D>>.() -> Unit) = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend GuildCommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A command that can only be executed in a DM.
 */
class DmCommand(override val names: List<String>, override var description: String) : Command(names, description) {
    /** @suppress */
    @NestedDSL
    fun execute(execute: suspend DmCommandEvent<NoArgs>.() -> Unit) = addExecution(listOf(), execute)

    /** @suppress */
    @NestedDSL
    fun <A> execute(a: ArgumentType<A>, execute: suspend DmCommandEvent<Args1<A>>.() -> Unit) = addExecution(listOf(a), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend DmCommandEvent<Args2<A, B>>.() -> Unit) = addExecution(listOf(a, b), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend DmCommandEvent<Args3<A, B, C>>.() -> Unit) = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend DmCommandEvent<Args4<A, B, C, D>>.() -> Unit) = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    @NestedDSL
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend DmCommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * Get a command by its name (case insensitive).
 */
operator fun MutableList<Command>.get(name: String) = firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
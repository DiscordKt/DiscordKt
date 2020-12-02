@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.internal.command.*

data class Execution<T : CommandEvent<*>>(val arguments: List<ArgumentType<*>>,
                                          val action: suspend T.() -> Unit,
                                          val isFlexible: Boolean = false) {
    val parameterCount: Int
        get() = arguments.size

    val signature
        get() = "(${arguments.joinToString { it.name }})"

    suspend fun execute(event: T) = action.invoke(event)
}

/**
 * @property names The name(s) this command can be executed by (case insensitive).
 * @property description A brief description of the command - used in documentation.
 * @property category The category that this command belongs to - set automatically by CommandSet.
 */
sealed class Command(open val names: List<String>,
                     open var description: String = "<No Description>") {
    var category: String = ""
    val executions: MutableList<Execution<*>> = mutableListOf()

    /**
     * Whether or not the command can parse the given arguments into a container.
     *
     * @param args The raw string arguments to be provided to the command.
     *
     * @return The result of the parsing operation.
     */
    suspend fun canParse(event: CommandEvent<*>, execution: Execution<*>, args: List<String>) = parseInputToBundle(this, execution, event, args) is ParseResult.Success

    /**
     * Invoke this command with the given args.
     */
    fun invoke(event: CommandEvent<TypeContainer>, args: List<String>) {
        GlobalScope.launch {
            val results = executions.map { it to parseInputToBundle(this@Command, it, event, args) }
            val success = results.firstOrNull { it.second is ParseResult.Success }

            if (success == null) {
                event.respond("Cannot execute ${event.rawInputs.commandName} with these args.")
                return@launch
            }

            event.args = (success.second as ParseResult.Success).argumentContainer
        }
    }

    protected fun <T : CommandEvent<*>> addExecution(argTypes: List<ArgumentType<*>>, event: suspend T.() -> Unit) {
        executions.add(Execution(argTypes, event))
    }
}

/**
 * A command that can be executed from anywhere.
 */
class GlobalCommand(override val names: List<String>,
                    override var description: String = "<No Description>") : Command(names, description) {
    /** @suppress */
    fun execute(execute: suspend CommandEvent<NoArgs>.() -> Unit) = addExecution(listOf(), execute)

    /** @suppress */
    fun <A> execute(a: ArgumentType<A>, execute: suspend CommandEvent<Args1<A>>.() -> Unit) = addExecution(listOf(a), execute)

    /** @suppress */
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend CommandEvent<Args2<A, B>>.() -> Unit) = addExecution(listOf(a, b), execute)

    /** @suppress */
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend CommandEvent<Args3<A, B, C>>.() -> Unit) = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend CommandEvent<Args4<A, B, C, D>>.() -> Unit) = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend CommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A command that can only be executed in a guild.
 */
class GuildCommand(override val names: List<String>,
                   override var description: String = "<No Description>") : Command(names, description) {
    /** @suppress */
    fun execute(execute: suspend GuildCommandEvent<NoArgs>.() -> Unit) = addExecution(listOf(), execute)

    /** @suppress */
    fun <A> execute(a: ArgumentType<A>, execute: suspend GuildCommandEvent<Args1<A>>.() -> Unit) = addExecution(listOf(a), execute)

    /** @suppress */
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend GuildCommandEvent<Args2<A, B>>.() -> Unit) = addExecution(listOf(a, b), execute)

    /** @suppress */
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend GuildCommandEvent<Args3<A, B, C>>.() -> Unit) = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend GuildCommandEvent<Args4<A, B, C, D>>.() -> Unit) = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend GuildCommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * A command that can only be executed in a DM.
 */
class DmCommand(override val names: List<String>,
                override var description: String = "<No Description>") : Command(names, description) {
    /** @suppress */
    fun execute(execute: suspend DmCommandEvent<NoArgs>.() -> Unit) = addExecution(listOf(), execute)

    /** @suppress */
    fun <A> execute(a: ArgumentType<A>, execute: suspend DmCommandEvent<Args1<A>>.() -> Unit) = addExecution(listOf(a), execute)

    /** @suppress */
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend DmCommandEvent<Args2<A, B>>.() -> Unit) = addExecution(listOf(a, b), execute)

    /** @suppress */
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend DmCommandEvent<Args3<A, B, C>>.() -> Unit) = addExecution(listOf(a, b, c), execute)

    /** @suppress */
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend DmCommandEvent<Args4<A, B, C, D>>.() -> Unit) = addExecution(listOf(a, b, c, d), execute)

    /** @suppress */
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend DmCommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = addExecution(listOf(a, b, c, d, e), execute)
}

/**
 * Get a command by its name (case insensitive).
 */
operator fun MutableList<Command>.get(name: String) = firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
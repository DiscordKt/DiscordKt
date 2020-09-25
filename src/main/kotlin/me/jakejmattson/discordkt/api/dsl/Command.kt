@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.internal.command.*

/**
 * @property names The name(s) this command can be executed by (case insensitive).
 * @property description A brief description of the command - used in documentation.
 * @property category The category that this command belongs to - set automatically by CommandSet.
 * @property isFlexible Whether or not this command can accept arguments in any order.
 * @property arguments The ArgumentTypes that are required by this function to execute.
 * @property execute The logic that will run whenever this command is executed.
 *
 * @property parameterCount The number of arguments this command accepts.
 */
interface Command {
    val names: List<String>
    var description: String
    var category: String
    var isFlexible: Boolean
    var arguments: List<ArgumentType<*>>
    //var execute: suspend (CommandEvent<*>) -> Unit

    val parameterCount: Int
        get() = arguments.size

    /**
     * Whether or not the command can parse the given arguments into a container.
     *
     * @param args The raw string arguments to be provided to the command.
     *
     * @return The result of the parsing operation.
     */
    suspend fun canParse(args: List<String>, event: GlobalCommandEvent<GenericContainer>) = parseInputToBundle(this, event, args) is ParseResult.Success

    /**
     * Execute this command with String arguments.
     */
    fun invoke(event: GlobalCommandEvent<GenericContainer>, args: List<String>)
}

class GlobalCommand(override val names: List<String>,
                    override var description: String = "<No Description>",
                    override var category: String = "",
                    override var isFlexible: Boolean = false,
                    override var arguments: List<ArgumentType<*>> = emptyList(),
                    private var execute: suspend (GlobalCommandEvent<*>) -> Unit = {}) : Command {

    override fun invoke(event: GlobalCommandEvent<GenericContainer>, args: List<String>) {
        GlobalScope.launch {
            when (val result = parseInputToBundle(this@GlobalCommand, event, args)) {
                is ParseResult.Success -> {
                    event.args = result.argumentContainer
                    execute.invoke(event)
                }
                is ParseResult.Error -> event.respond(result.reason)
            }
        }
    }

    private fun <T : GenericContainer> setExecute(argTypes: List<ArgumentType<*>>, event: suspend (GlobalCommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as suspend (GlobalCommandEvent<*>) -> Unit
    }

    fun execute(execute: suspend GlobalCommandEvent<NoArgs>.() -> Unit) = setExecute(listOf(), execute)
    fun <A> execute(a: ArgumentType<A>, execute: suspend GlobalCommandEvent<Args1<A>>.() -> Unit) = setExecute(listOf(a), execute)
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend GlobalCommandEvent<Args2<A, B>>.() -> Unit) = setExecute(listOf(a, b), execute)
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend GlobalCommandEvent<Args3<A, B, C>>.() -> Unit) = setExecute(listOf(a, b, c), execute)
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend GlobalCommandEvent<Args4<A, B, C, D>>.() -> Unit) = setExecute(listOf(a, b, c, d), execute)
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend GlobalCommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = setExecute(listOf(a, b, c, d, e), execute)
}

class GuildCommand(override val names: List<String>,
                   override var description: String = "<No Description>",
                   override var category: String = "",
                   override var isFlexible: Boolean = false,
                   override var arguments: List<ArgumentType<*>> = emptyList(),
                   private var execute: suspend (GuildCommandEvent<*>) -> Unit = {}) : Command {

    override fun invoke(event: GlobalCommandEvent<GenericContainer>, args: List<String>) {
        GlobalScope.launch {
            when (val result = parseInputToBundle(this@GuildCommand, event, args)) {
                is ParseResult.Success -> {
                    event.args = result.argumentContainer
                    execute.invoke(event.toGuildEvent())
                }
                is ParseResult.Error -> event.respond(result.reason)
            }
        }
    }

    private fun <T : GenericContainer> setExecute(argTypes: List<ArgumentType<*>>, event: suspend (GuildCommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as suspend (GuildCommandEvent<*>) -> Unit
    }

    fun execute(execute: suspend GuildCommandEvent<NoArgs>.() -> Unit) = setExecute(listOf(), execute)
    fun <A> execute(a: ArgumentType<A>, execute: suspend GuildCommandEvent<Args1<A>>.() -> Unit) = setExecute(listOf(a), execute)
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend GuildCommandEvent<Args2<A, B>>.() -> Unit) = setExecute(listOf(a, b), execute)
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend GuildCommandEvent<Args3<A, B, C>>.() -> Unit) = setExecute(listOf(a, b, c), execute)
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend GuildCommandEvent<Args4<A, B, C, D>>.() -> Unit) = setExecute(listOf(a, b, c, d), execute)
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend GuildCommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = setExecute(listOf(a, b, c, d, e), execute)
}

class DmCommand(override val names: List<String>,
                override var description: String = "<No Description>",
                override var category: String = "",
                override var isFlexible: Boolean = false,
                override var arguments: List<ArgumentType<*>> = emptyList(),
                private var execute: suspend (DmCommandEvent<*>) -> Unit = {}) : Command {

    override fun invoke(event: GlobalCommandEvent<GenericContainer>, args: List<String>) {
        GlobalScope.launch {
            when (val result = parseInputToBundle(this@DmCommand, event, args)) {
                is ParseResult.Success -> {
                    event.args = result.argumentContainer
                    execute.invoke(event.toDmEvent())
                }
                is ParseResult.Error -> event.respond(result.reason)
            }
        }
    }

    private fun <T : GenericContainer> setExecute(argTypes: List<ArgumentType<*>>, event: suspend (DmCommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as suspend (DmCommandEvent<*>) -> Unit
    }

    fun execute(execute: suspend DmCommandEvent<NoArgs>.() -> Unit) = setExecute(listOf(), execute)
    fun <A> execute(a: ArgumentType<A>, execute: suspend DmCommandEvent<Args1<A>>.() -> Unit) = setExecute(listOf(a), execute)
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend DmCommandEvent<Args2<A, B>>.() -> Unit) = setExecute(listOf(a, b), execute)
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend DmCommandEvent<Args3<A, B, C>>.() -> Unit) = setExecute(listOf(a, b, c), execute)
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend DmCommandEvent<Args4<A, B, C, D>>.() -> Unit) = setExecute(listOf(a, b, c, d), execute)
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend DmCommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = setExecute(listOf(a, b, c, d, e), execute)
}

/**
 * Get a command by its name (case insensitive).
 */
operator fun MutableList<Command>.get(name: String) = firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
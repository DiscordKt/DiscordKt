@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.command

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.api.dsl.arguments.ArgumentType
import me.jakejmattson.discordkt.internal.command.*

/**
 * @param names The name(s) this command can be executed by (case insensitive).
 * @param description A brief description of the command - used in documentation.
 * @param category The category that this command belongs to - set automatically by CommandSet.
 * @param requiresGuild Whether or not this command needs to be executed in a guild.
 * @param isFlexible Whether or not this command can accept arguments in any order.
 * @param arguments The ArgumentTypes that are required by this function to execute.
 * @param execute The logic that will run whenever this command is executed.
 *
 * @property parameterCount The number of arguments this command accepts.
 */
class Command(val names: List<String>,
              var description: String = "<No Description>",
              var category: String = "",
              var requiresGuild: Boolean? = null,
              var isFlexible: Boolean = false,
              var arguments: List<ArgumentType<*>> = emptyList(),
              private var execute: suspend (CommandEvent<*>) -> Unit = {}) {

    val parameterCount: Int
        get() = arguments.size

    /**
     * Whether or not the command can parse the given arguments into a container.
     *
     * @param args The raw string arguments to be provided to the command.
     *
     * @return The result of the parsing operation.
     */
    suspend fun canParse(args: List<String>, event: CommandEvent<GenericContainer>) = parseInputToBundle(this, event, args) is ParseResult.Success

    /**
     * Invoke this command "blindly" with the given arguments and context.
     *
     * @param args The raw string arguments to be provided to the command.
     */
    fun invoke(event: CommandEvent<GenericContainer>, args: List<String>) {
        GlobalScope.launch {
            when (val result = parseInputToBundle(this@Command, event, args)) {
                is ParseResult.Success -> {
                    event.args = result.argumentContainer
                    execute.invoke(event)
                }
                is ParseResult.Error -> event.respond(result.reason)
            }
        }
    }

    private fun <T : GenericContainer> setExecute(argTypes: List<ArgumentType<*>>, event: suspend (CommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as suspend (CommandEvent<*>) -> Unit
    }

    /** The logic run when this command is invoked */
    fun execute(execute: suspend CommandEvent<NoArgs>.() -> Unit) = setExecute(listOf(), execute)

    /** The logic run when this command is invoked */
    fun <A> execute(a: ArgumentType<A>, execute: suspend CommandEvent<Args1<A>>.() -> Unit) = setExecute(listOf(a), execute)

    /** The logic run when this command is invoked */
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend CommandEvent<Args2<A, B>>.() -> Unit) = setExecute(listOf(a, b), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend CommandEvent<Args3<A, B, C>>.() -> Unit) = setExecute(listOf(a, b, c), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend CommandEvent<Args4<A, B, C, D>>.() -> Unit) = setExecute(listOf(a, b, c, d), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend CommandEvent<Args5<A, B, C, D, E>>.() -> Unit) = setExecute(listOf(a, b, c, d, e), execute)
}

/**
 * Get a command by its name (case insensitive).
 */
operator fun MutableList<Command>.get(name: String) = firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
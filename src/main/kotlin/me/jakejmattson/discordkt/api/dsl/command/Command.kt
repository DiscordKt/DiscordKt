@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.command

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.internal.command.*
import me.jakejmattson.discordkt.internal.utils.*

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
              var arguments: List<Arg<*>> = emptyList(),
              private var execute: (CommandEvent<*>) -> Unit = {}) {

    val parameterCount: Int
        get() = arguments.size

    /**
     * Manually parse args into an intermediate bundle to allow verifying the result, then execute it with [manualInvoke].
     *
     * @param args The raw string arguments to be provided to the command.
     * @return The result of the parsing operation as a [ParseResult].
     *
     * TODO See if this is still possible
     */
    //suspend fun manualParseInput(args: List<String>, event: CommandEvent<GenericContainer>) = parseInputToBundle(this, args, event)

    /**
     * Invoke this command manually with the parsed output from [manualParseInput].
     *
     * @param parsedData String arguments parsed into their respective types and bundled into a GenericContainer.
     */
    /**
    fun manualInvoke(parsedData: GenericContainer, event: CommandEvent<GenericContainer>) {
    GlobalScope.launch {
    event.args = parsedData
    execute.invoke(event)
    }
    }
     */

    /**
     * Invoke this command "blindly" with the given arguments and context. Use [manualParseInput] and [manualInvoke] for a manual approach.
     *
     * @param args The raw string arguments to be provided to the command.
     */
    fun invoke(args: List<String>, event: CommandEvent<GenericContainer>) {
        GlobalScope.launch {
            when (val result = parseInputToBundle(this@Command, args, event)) {
                is ParseResult.Success -> {
                    event.args = result.argumentContainer
                    execute.invoke(event)
                }
                is ParseResult.Error -> event.respond(result.reason)
            }
        }
    }

    private fun <T : GenericContainer> setExecute(argTypes: List<Arg<*>>, event: (CommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as (CommandEvent<*>) -> Unit
    }

    /** The logic run when this command is invoked */
    fun execute(execute: execute0) = setExecute(listOf(), execute)

    /** The logic run when this command is invoked */
    fun <A> execute(a1: Arg<A>, execute: execute1<A>) = GlobalScope.launch { setExecute(listOf(a1), execute) }

    /** The logic run when this command is invoked */
    fun <A, B> execute(a1: Arg<A>, a2: Arg<B>, execute: execute2<A, B>) = setExecute(listOf(a1, a2), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, execute: execute3<A, B, C>) = setExecute(listOf(a1, a2, a3), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C, D> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, a4: Arg<D>, execute: execute4<A, B, C, D>) = setExecute(listOf(a1, a2, a3, a4), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C, D, E> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, a4: Arg<D>, a5: Arg<E>, execute: execute5<A, B, C, D, E>) = setExecute(listOf(a1, a2, a3, a4, a5), execute)
}
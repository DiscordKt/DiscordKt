@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.command

import kotlinx.coroutines.*
import me.jakejmattson.kutils.api.annotations.KutilsDsl
import me.jakejmattson.kutils.internal.command.*
import me.jakejmattson.kutils.internal.utils.*

@KutilsDsl
/**
 * @param names The name(s) this command can be executed by (case insensitive).
 * @param description A brief description of the command - used in documentation.
 * @param category The category that this command belongs to - set automatically by CommandSet.
 * @param requiresGuild Whether or not this command needs to be executed in a guild.
 * @param isFlexible Whether or not this command can accept arguments in any order.
 * @param arguments The ArgumentTypes that are required by this function to execute.
 * @param execute The logic that will run whenever this command is executed.
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
     * Manually parse args into an intermediate bundle to allow verifying the result.
     *
     * @param args The raw string arguments to be provided to the command.
     * @return The result of the parsing operation as a ParseResult
     * @see manualInvoke
     * @see ParseResult
     */
    fun manualParseInput(args: List<String>, event: CommandEvent<GenericContainer>) = parseInputToBundle(this, args, event)

    /**
     * Invoke this command manually.
     *
     * @param parsedData String arguments parsed into their respective types and bundled into a GenericContainer.
     * @see manualParseInput
     */
    fun manualInvoke(parsedData: GenericContainer, event: CommandEvent<GenericContainer>) {
        GlobalScope.launch {
            event.args = parsedData
            execute.invoke(event)
        }
    }

    /**
     * Invoke this command "blindly" with the given arguments and context.
     *
     * @param args The raw string arguments to be provided to the command.
     * @see manualParseInput
     */
    fun invoke(args: List<String>, event: CommandEvent<GenericContainer>) {
        GlobalScope.launch {
            val result = parseInputToBundle(this@Command, args, event)

            when (result) {
                is ParseResult.Success -> {
                    event.args = result.argumentContainer
                    execute.invoke(event)
                }
                is ParseResult.Error -> {
                    val error = result.error

                    with(event) {
                        if (discord.configuration.deleteErrors) respondTimed(error) else respond(error)
                    }
                }
            }
        }
    }

    private fun <T : GenericContainer> setExecute(argTypes: List<Arg<*>>, event: (CommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as (CommandEvent<*>) -> Unit
    }

    fun execute(execute: execute0) = setExecute(listOf(), execute)
    fun <A> execute(a1: Arg<A>, execute: execute1<A>) = setExecute(listOf(a1), execute)
    fun <A, B> execute(a1: Arg<A>, a2: Arg<B>, execute: execute2<A, B>) = setExecute(listOf(a1, a2), execute)
    fun <A, B, C> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, execute: execute3<A, B, C>) = setExecute(listOf(a1, a2, a3), execute)
    fun <A, B, C, D> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, a4: Arg<D>, execute: execute4<A, B, C, D>) = setExecute(listOf(a1, a2, a3, a4), execute)
    fun <A, B, C, D, E> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, a4: Arg<D>, a5: Arg<E>, execute: execute5<A, B, C, D, E>) = setExecute(listOf(a1, a2, a3, a4, a5), execute)
}
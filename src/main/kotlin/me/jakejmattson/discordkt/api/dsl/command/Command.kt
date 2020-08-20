@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.command

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
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

    private fun <T : GenericContainer> setExecute(argTypes: List<Arg<*>>, event: (CommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as (CommandEvent<*>) -> Unit
    }

    /** The logic run when this command is invoked */
    fun execute(execute: execute0) = setExecute(listOf(), execute)

    /** The logic run when this command is invoked */
    fun <A> execute(a1: Arg<A>, execute: execute1<A>) = setExecute(listOf(a1), execute)

    /** The logic run when this command is invoked */
    fun <A, B> execute(a1: Arg<A>, a2: Arg<B>, execute: execute2<A, B>) = setExecute(listOf(a1, a2), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, execute: execute3<A, B, C>) = setExecute(listOf(a1, a2, a3), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C, D> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, a4: Arg<D>, execute: execute4<A, B, C, D>) = setExecute(listOf(a1, a2, a3, a4), execute)

    /** The logic run when this command is invoked */
    fun <A, B, C, D, E> execute(a1: Arg<A>, a2: Arg<B>, a3: Arg<C>, a4: Arg<D>, a5: Arg<E>, execute: execute5<A, B, C, D, E>) = setExecute(listOf(a1, a2, a3, a4, a5), execute)
}

/**
 * Create a block where multiple commands can be created.
 */
@BuilderDSL
fun commands(construct: MutableList<Command>.() -> Unit): MutableList<Command> {
    val commands = mutableListOf<Command>()
    commands.construct()
    return commands
}

/**
 * Create a new command in this list.
 */
fun MutableList<Command>.command(vararg names: String, body: Command.() -> Unit) {
    val command = Command(names.toList())
    command.body()
    add(command)
}

operator fun MutableList<Command>.get(name: String) = firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
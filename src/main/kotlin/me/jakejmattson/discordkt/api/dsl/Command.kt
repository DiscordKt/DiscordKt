@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.api.*
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.internal.command.*
import kotlin.reflect.KClass

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
class Command<T : CommandEvent>(private val type: KClass<T>,
                                val names: List<String>,
                                var description: String = "<No Description>",
                                var isFlexible: Boolean = false) {

    internal fun isGuildViable() = type != DmCommandEvent::class
    internal fun isDmViable() = type != GuildCommandEvent::class

    var category: String = ""
    var arguments: List<ArgumentType<*>> = emptyList()
    private var execute: suspend CommandEvent.(GenericContainer) -> Unit = {}

    val parameterCount: Int
        get() = arguments.size

    /**
     * Whether or not the command can parse the given arguments into a container.
     *
     * @param args The raw string arguments to be provided to the command.
     *
     * @return The result of the parsing operation.
     */
    suspend fun canParse(args: List<String>, event: CommandEvent) = parseInputToBundle(this, event, args) is ParseResult.Success

    fun invoke(event: CommandEvent, args: List<String>) {
        GlobalScope.launch {
            when (val result = parseInputToBundle(this@Command, event, args)) {
                is ParseResult.Success -> execute.invoke(event, result.argumentContainer)
                is ParseResult.Error -> event.respond(result.reason)
            }
        }
    }

    private fun <C: GenericContainer> setExecute(argTypes: List<ArgumentType<*>>, event: suspend T.(C) -> Unit) {
        arguments = argTypes
        execute = event as suspend CommandEvent.(GenericContainer) -> Unit
    }

    fun execute(execute: suspend T.(NoArgs) -> Unit) = setExecute(listOf(), execute)
    fun <A> execute(a: ArgumentType<A>, execute: suspend T.(Args1<A>) -> Unit) = setExecute(listOf(a), execute)
    fun <A, B> execute(a: ArgumentType<A>, b: ArgumentType<B>, execute: suspend T.(Args2<A, B>) -> Unit) = setExecute(listOf(a, b), execute)
    fun <A, B, C> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, execute: suspend T.(Args3<A, B, C>) -> Unit) = setExecute(listOf(a, b, c), execute)
    fun <A, B, C, D> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, execute: suspend T.(Args4<A, B, C, D>) -> Unit) = setExecute(listOf(a, b, c, d), execute)
    fun <A, B, C, D, E> execute(a: ArgumentType<A>, b: ArgumentType<B>, c: ArgumentType<C>, d: ArgumentType<D>, e: ArgumentType<E>, execute: suspend T.(Args5<A, B, C, D, E>) -> Unit) = setExecute(listOf(a, b, c, d, e), execute)
}

/**
 * Get a command by its name (case insensitive).
 */
operator fun MutableList<Command<*>>.get(name: String) = firstOrNull { name.toLowerCase() in it.names.map { it.toLowerCase() } }
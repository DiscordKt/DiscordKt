package me.aberrantfox.kutils.api.dsl.command

import me.aberrantfox.kutils.api.dsl.arguments.ArgumentType

@CommandTagMarker
class Command(val names: List<String>,
              var description: String = "<No Description>",
              var category: String = "",
              var requiresGuild: Boolean? = null,
              var isFlexible: Boolean = false,
              var arguments: List<ArgumentType<*>> = emptyList(),
              private var execute: (CommandEvent<*>) -> Unit = {}) {

    val parameterCount: Int
        get() = arguments.size

    fun invoke(parsedData: GenericContainer, event: CommandEvent<GenericContainer>) {
        event.args = parsedData
        execute.invoke(event)
    }

    private fun <T : GenericContainer> execute(argTypes: List<ArgumentType<*>>, event: (CommandEvent<T>) -> Unit) {
        arguments = argTypes
        execute = event as (CommandEvent<*>) -> Unit
    }

    fun execute(execute: (CommandEvent<NoArgs>) -> Unit) = execute(listOf(), execute)
    fun <A> execute(a1: ArgumentType<A>, execute: (CommandEvent<Args1<A>>) -> Unit) = execute(listOf(a1), execute)
    fun <A, B> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, execute: (CommandEvent<Args2<A, B>>) -> Unit) = execute(listOf(a1, a2), execute)
    fun <A, B, C> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, a3: ArgumentType<C>, execute: (CommandEvent<Args3<A, B, C>>) -> Unit) = execute(listOf(a1, a2, a3), execute)
    fun <A, B, C, D> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, a3: ArgumentType<C>, a4: ArgumentType<D>, execute: (CommandEvent<Args4<A, B, C, D>>) -> Unit) = execute(listOf(a1, a2, a3, a4), execute)
    fun <A, B, C, D, E> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, a3: ArgumentType<C>, a4: ArgumentType<D>, a5: ArgumentType<E>, execute: (CommandEvent<Args5<A, B, C, D, E>>) -> Unit) = execute(listOf(a1, a2, a3, a4, a5), execute)
}
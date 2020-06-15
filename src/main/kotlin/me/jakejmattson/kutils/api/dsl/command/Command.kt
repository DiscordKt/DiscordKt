@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.command

import me.jakejmattson.kutils.api.annotations.KutilsDsl
import me.jakejmattson.kutils.internal.utils.*

@KutilsDsl
class Command(val names: List<String>,
              var description: String = "<No Description>",
              var category: String = "",
              var requiresGuild: Boolean? = null,
              var isFlexible: Boolean = false,
              var arguments: List<Arg<*>> = emptyList(),
              private var execute: (CommandEvent<*>) -> Unit = {}) {

    val parameterCount: Int
        get() = arguments.size

    fun invoke(parsedData: GenericContainer, event: CommandEvent<GenericContainer>) {
        event.args = parsedData
        execute.invoke(event)
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
package me.aberrantfox.kjdautils.api.dsl.command

import me.aberrantfox.kjdautils.internal.command.ArgumentType

@CommandTagMarker
class Command(val names: List<String>,
              var description: String = "<No Description>",
              var category: String = "",
              var requiresGuild: Boolean = false,
              var isFlexible: Boolean = false,
              var expectedArgs: ArgumentCollection = ArgumentCollection(),
              private var execute: (CommandEvent<*>) -> Unit = {}) {

    val parameterCount: Int
        get() = expectedArgs.size

    fun invoke(parsedData: GenericContainer, event: CommandEvent<GenericContainer>) {
        event.args = parsedData
        execute.invoke(event)
    }

    private fun <T : GenericContainer> execute(collection: ArgumentCollection, event: (CommandEvent<T>) -> Unit) {
        expectedArgs = collection
        execute = event as (CommandEvent<*>) -> Unit
    }

    fun execute(execute: (CommandEvent<NoArgs>) -> Unit) = execute(ArgumentCollection(), execute)
    fun <A> execute(a1: ArgumentType<A>, execute: (CommandEvent<Args1<A>>) -> Unit) = execute(ArgumentCollection(a1), execute)
    fun <A, B> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, execute: (CommandEvent<Args2<A, B>>) -> Unit) = execute(ArgumentCollection(a1, a2), execute)
    fun <A, B, C> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, a3: ArgumentType<C>, execute: (CommandEvent<Args3<A, B, C>>) -> Unit) = execute(ArgumentCollection(a1, a2, a3), execute)

    fun <A, B, C, D> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, a3: ArgumentType<C>, a4: ArgumentType<D>, execute: (CommandEvent<Args4<A, B, C, D>>) -> Unit) =
        execute(ArgumentCollection(a1, a2, a3, a4), execute)

    fun <A, B, C, D, E> execute(a1: ArgumentType<A>, a2: ArgumentType<B>, a3: ArgumentType<C>, a4: ArgumentType<D>, a5: ArgumentType<E>, execute: (CommandEvent<Args5<A, B, C, D, E>>) -> Unit) =
        execute(ArgumentCollection(a1, a2, a3, a4, a5), execute)
}
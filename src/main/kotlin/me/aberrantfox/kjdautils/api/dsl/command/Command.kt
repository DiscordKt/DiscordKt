package me.aberrantfox.kjdautils.api.dsl.command

import me.aberrantfox.kjdautils.internal.command.ArgumentType

@CommandTagMarker
class Command(val names: List<String>,
              var category: String = "",
              var expectedArgs: ArgumentCollection<*> = args(),
              private var execute: (CommandEvent<*>) -> Unit = {},
              var requiresGuild: Boolean = false,
              var description: String = "No Description Provider") {

    val parameterCount: Int
        get() = this.expectedArgs.size

    fun invoke(parsedData: ArgumentContainer, event: CommandEvent<ArgumentContainer>) {
        event.args = parsedData
        execute.invoke(event)
    }

    fun <T : ArgumentContainer> execute(collection: ArgumentCollection<*>, event: (CommandEvent<T>) -> Unit) {
        expectedArgs = collection
        this.execute = event as (CommandEvent<*>) -> Unit
    }

    fun execute(execute: (CommandEvent<NoArg>) -> Unit) {
        execute(args(), execute)
    }

    fun<T> execute(argument: ArgumentType<T>,
                   execute: (CommandEvent<SingleArg<T>>) -> Unit) {
        execute(args(argument), execute)
    }

    fun<A, B> execute(first: ArgumentType<A>,
                      second: ArgumentType<B>,
                      execute: (CommandEvent<DoubleArg<A, B>>) -> Unit) {
        execute(args(first, second), execute)
    }

    fun<A, B, C> execute(first: ArgumentType<A>,
                         second: ArgumentType<B>,
                         third: ArgumentType<C>,
                         execute: (CommandEvent<TripleArg<A, B, C>>) -> Unit) {
        execute(args(first, second, third), execute)
    }

    fun<A, B, C, D> execute(first: ArgumentType<A>,
                            second: ArgumentType<B>,
                            third: ArgumentType<C>,
                            fourth: ArgumentType<D>,
                            execute: (CommandEvent<QuadArg<A, B, C, D>>) -> Unit) {
        execute(args(first, second, third, fourth), execute)
    }
}
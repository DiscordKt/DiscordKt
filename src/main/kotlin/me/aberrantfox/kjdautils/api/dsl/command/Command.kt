package me.aberrantfox.kjdautils.api.dsl.command

import me.aberrantfox.kjdautils.internal.businessobjects.CommandData
import me.aberrantfox.kjdautils.internal.command.ArgumentType

@CommandTagMarker
class Command(val name: String,
              var category: String = "",
              var expectedArgs: ArgumentCollection<*> = args(),
              private var execute: (CommandEvent<*>) -> Unit = {},
              var requiresGuild: Boolean = false,
              var description: String = "No Description Provider") {

    fun invoke(parsedData: ArgumentContainer, event: CommandEvent<ArgumentContainer>) {
        event.args = parsedData
        execute.invoke(event)
    }

    val parameterCount: Int
        get() = this.expectedArgs.size

    fun<T : ArgumentContainer> execute(collection: ArgumentCollection<*>, event: (CommandEvent<T>) -> Unit) {
        expectedArgs = collection
        this.execute = event as (CommandEvent<*>) -> Unit
    }

    fun toCommandData(): CommandData {
        val expectedArgs = expectedArgs.arguments.joinToString {
            if (it.isOptional) "(${it.name})" else it.name
        }.takeIf { it.isNotEmpty() } ?: "<none>"

        return CommandData(name.replace("|", "\\|"),
            expectedArgs.replace("|", "\\|"),
            description.replace("|", "\\|"))
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
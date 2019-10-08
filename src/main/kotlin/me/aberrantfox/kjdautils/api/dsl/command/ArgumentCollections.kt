package me.aberrantfox.kjdautils.api.dsl.command

import me.aberrantfox.kjdautils.internal.command.ArgumentType

open class ArgumentContainer
class NoArg: ArgumentContainer()
data class SingleArg<T>(val first: T): ArgumentContainer()
data class DoubleArg<A, B>(val first: A, val second: B): ArgumentContainer()
data class TripleArg<A, B, C>(val first: A, val second: B, val third: C): ArgumentContainer()
data class QuadArg<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D): ArgumentContainer()

interface ArgumentCollection<T : ArgumentContainer> {
    val arguments: List<ArgumentType<*>>

    val size: Int
        get() = arguments.size

    fun bundle(arguments: List<Any>): T
}

fun args() =
    object : ArgumentCollection<NoArg> {
        override val arguments: List<ArgumentType<Nothing>>
            get() = listOf()

        override fun bundle(arguments: List<Any>) = NoArg()
    }

fun <A> args(first: ArgumentType<A>) =
    object : ArgumentCollection<SingleArg<A>> {
        override val arguments: List<ArgumentType<*>>
            get() = listOf(first)

        override fun bundle(arguments: List<Any>): SingleArg<A> {
            return SingleArg(arguments[0]) as SingleArg<A>
        }
    }

fun <A, B> args(first: ArgumentType<A>, second: ArgumentType<B>) =
    object : ArgumentCollection<DoubleArg<A, B>> {
        override val arguments: List<ArgumentType<*>>
            get() = listOf(first, second)

        override fun bundle(arguments: List<Any>): DoubleArg<A, B> {
            return DoubleArg(arguments[0], arguments[1]) as DoubleArg<A, B>
        }
    }

fun <A, B, C> args(first: ArgumentType<A>, second: ArgumentType<B>, third: ArgumentType<C>) =
    object : ArgumentCollection<TripleArg<A, B, C>> {
        override val arguments: List<ArgumentType<*>>
            get() = listOf(first, second, third)

        override fun bundle(arguments: List<Any>): TripleArg<A, B, C> {
            return TripleArg(arguments[0], arguments[1], arguments[2]) as TripleArg<A, B, C>
        }
    }

fun <A, B, C, D> args(first: ArgumentType<A>, second: ArgumentType<B>, third: ArgumentType<C>, fourth: ArgumentType<D>) =
    object : ArgumentCollection<QuadArg<A, B, C, D>> {
        override val arguments: List<ArgumentType<*>>
            get() = listOf(first, second, third, fourth)

        override fun bundle(arguments: List<Any>): QuadArg<A, B, C, D> {
            return QuadArg(arguments[0], arguments[1], arguments[2], arguments[3]) as QuadArg<A, B, C, D>
        }
    }
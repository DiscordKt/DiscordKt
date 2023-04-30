package me.jakejmattson.discordkt

import me.jakejmattson.discordkt.util.stringify

/** A container for data that saves type information. */
public interface TypeContainer {
    /**
     * Call [stringify] on all args and add them to a list.
     *
     * @return The list of args
     */
    public fun toList(): List<String>
}

public class NoArgs : TypeContainer {
    override fun toList(): List<String> = emptyList()
}

public data class Args1<A>(val first: A) : TypeContainer {
    override fun toList(): List<String> = listOf(stringify(first))
}

public data class Args2<A, B>(val first: A, val second: B) : TypeContainer {
    override fun toList(): List<String> = listOf(
        stringify(first), stringify(second)
    )
}

public data class Args3<A, B, C>(val first: A, val second: B, val third: C) : TypeContainer {
    override fun toList(): List<String> = listOf(
        stringify(first), stringify(second), stringify(third)
    )
}

public data class Args4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) : TypeContainer {
    override fun toList(): List<String> = listOf(
        stringify(first), stringify(second), stringify(third), stringify(fourth)
    )
}

public data class Args5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E) :
    TypeContainer {
    override fun toList(): List<String> = listOf(
        stringify(first), stringify(second), stringify(third), stringify(fourth), stringify(fifth)
    )
}

internal fun bundleToContainer(arguments: List<Any?>) = when (arguments.size) {
    0 -> NoArgs()
    1 -> Args1(arguments[0])
    2 -> Args2(arguments[0], arguments[1])
    3 -> Args3(arguments[0], arguments[1], arguments[2])
    4 -> Args4(arguments[0], arguments[1], arguments[2], arguments[3])
    5 -> Args5(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4])
    else -> throw IllegalArgumentException("Cannot handle (${arguments.size}) arguments.")
}
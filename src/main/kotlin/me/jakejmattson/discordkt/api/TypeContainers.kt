package me.jakejmattson.discordkt.api

/** A container for data that saves type information. */
interface TypeContainer

class NoArgs : TypeContainer
data class Args1<A>(val first: A) : TypeContainer
data class Args2<A, B>(val first: A, val second: B) : TypeContainer
data class Args3<A, B, C>(val first: A, val second: B, val third: C) : TypeContainer
data class Args4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) : TypeContainer
data class Args5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E) : TypeContainer

internal fun bundleToContainer(arguments: List<Any?>) =
    when (arguments.size) {
        0 -> NoArgs()
        1 -> Args1(arguments[0])
        2 -> Args2(arguments[0], arguments[1])
        3 -> Args3(arguments[0], arguments[1], arguments[2])
        4 -> Args4(arguments[0], arguments[1], arguments[2], arguments[3])
        5 -> Args5(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4])
        else -> throw IllegalArgumentException("Cannot handle (${arguments.size}) arguments.")
    }
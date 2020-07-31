package me.jakejmattson.discordkt.api.dsl.command

/** A container for generic data that saves type information. */
open class GenericContainer

/** @suppress GenericContainer */
class NoArgs : GenericContainer()

/** @suppress GenericContainer */
data class Args1<T>(val first: T) : GenericContainer()

/** @suppress GenericContainer */
data class Args2<A, B>(val first: A, val second: B) : GenericContainer()

/** @suppress GenericContainer */
data class Args3<A, B, C>(val first: A, val second: B, val third: C) : GenericContainer()

/** @suppress GenericContainer */
data class Args4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) : GenericContainer()

/** @suppress GenericContainer */
data class Args5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E) : GenericContainer()

internal fun bundleToArgContainer(arguments: List<Any>) =
    when (arguments.size) {
        0 -> NoArgs()
        1 -> Args1(arguments[0])
        2 -> Args2(arguments[0], arguments[1])
        3 -> Args3(arguments[0], arguments[1], arguments[2])
        4 -> Args4(arguments[0], arguments[1], arguments[2], arguments[3])
        5 -> Args5(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4])
        else -> throw IllegalArgumentException("Cannot handle (${arguments.size}) arguments.")
    }
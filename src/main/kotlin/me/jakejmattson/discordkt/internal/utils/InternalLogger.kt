package me.jakejmattson.discordkt.internal.utils

import kotlin.system.exitProcess

@PublishedApi
internal object InternalLogger {
    fun log(message: String) {
        println(message)
    }

    fun error(message: String) {
        System.err.println(message)
    }

    fun fatalError(message: String) {
        System.err.println("[FATAL] $message")
        exitProcess(-1)
    }
}
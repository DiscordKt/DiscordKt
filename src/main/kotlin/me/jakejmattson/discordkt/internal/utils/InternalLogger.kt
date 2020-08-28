package me.jakejmattson.discordkt.internal.utils

internal class InternalLogger {
    companion object {
        fun log(message: String) {
            println(message)
        }

        fun error(message: String) {
            System.err.println(message)
        }
    }
}
package me.jakejmattson.discordkt.internal.utils

internal class InternalLogger {
    companion object {
        var shouldLogStartup = true

        fun startup(message: String) {
            if (shouldLogStartup)
                println(message)
        }

        fun error(message: String) {
            System.err.println(message)
        }
    }
}
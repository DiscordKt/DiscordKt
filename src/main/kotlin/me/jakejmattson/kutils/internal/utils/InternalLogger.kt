package me.jakejmattson.kutils.internal.utils

internal class InternalLogger {
    companion object {
        var shouldLogStartup = true

        fun startup(message: String) {
            if (shouldLogStartup)
                println(message)
        }

        fun info(message: String) {
            println("KUtils: $message")
        }

        fun error(message: String) {
            System.err.println(message)
        }
    }
}
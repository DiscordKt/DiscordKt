package me.jakejmattson.kutils.internal.utils

internal class InternalLogger {
    companion object {
        fun startup(message: String) {
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
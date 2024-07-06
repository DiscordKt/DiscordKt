package me.jakejmattson.discordkt.internal.utils

import org.slf4j.Logger
import kotlin.system.exitProcess

@PublishedApi
internal object InternalLogger {

    private var loggerSet: Boolean = false
    var logger: Logger? = null
        set(value) {
            field = value
            loggerSet = true
        }

    fun log(message: String) {
        if (loggerSet) {
            logger!!.info(message)
            return
        }
        println(message)
    }

    fun error(message: String) {
        if (loggerSet) {
            logger!!.error(message)
            return
        }
        System.err.println(message)
    }

    fun fatalError(message: String) {
        if (loggerSet) {
            logger!!.error("[FATAL] $message")
            exitProcess(-1)
        }
        System.err.println("[FATAL] $message")
        exitProcess(-1)
    }
}
@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl

import me.jakejmattson.kutils.internal.utils.KUtils
import org.slf4j.simple.SimpleLogger

fun bot(token: String, operate: KUtils.() -> Unit): KUtils {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN")

    val util = KUtils(token, defaultGlobalPath(Exception()))
    util.operate()
    util.buildBot()

    return util
}

private fun defaultGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    val lastIndex = full.lastIndexOf(".").takeIf { it != -1 } ?: full.lastIndex
    return full.substring(0, lastIndex)
}
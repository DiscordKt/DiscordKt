package me.jakejmattson.kutils.api.dsl.configuration

import me.jakejmattson.kutils.internal.utils.*
import org.slf4j.impl.SimpleLogger

fun startBot(token: String, enableScriptEngine: Boolean = false, globalPath: String = defaultGlobalPath(Exception()), operate: KUtils.() -> Unit = {}): KUtils {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN")

    val util = KUtils(KConfiguration(), token, globalPath, enableScriptEngine)
    util.operate()

    InternalLogger.startup("----------------------------------------------")
    return util
}

private fun defaultGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    val lastIndex = full.lastIndexOf(".").takeIf { it != -1 } ?: full.lastIndex
    return full.substring(0, lastIndex)
}
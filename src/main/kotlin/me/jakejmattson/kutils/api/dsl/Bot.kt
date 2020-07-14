@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl

import me.jakejmattson.kutils.internal.utils.KUtils

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [configure][KUtils.configure], [injection][KUtils.injection], [client][KUtils.client], [logging][KUtils.logging]
 *
 * @param token Your Discord bot token.
 */
fun bot(token: String, operate: KUtils.() -> Unit): KUtils {
    val util = KUtils(token, detectGlobalPath(Exception()))
    util.operate()
    util.buildBot()

    return util
}

private fun detectGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    val lastIndex = full.lastIndexOf(".").takeIf { it != -1 } ?: full.lastIndex
    return full.substring(0, lastIndex)
}
@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl

import me.jakejmattson.kutils.internal.utils.Bot

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [configure][Bot.configure], [injection][Bot.injection], [client][Bot.client], [logging][Bot.logging]
 *
 * @param token Your Discord bot token.
 */
fun bot(token: String, operate: Bot.() -> Unit) =
    Bot(token, detectGlobalPath(Exception())).apply {
        operate()
        buildBot()
    }

private fun detectGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    val lastIndex = full.lastIndexOf(".").takeIf { it != -1 } ?: full.lastIndex
    return full.substring(0, lastIndex)
}
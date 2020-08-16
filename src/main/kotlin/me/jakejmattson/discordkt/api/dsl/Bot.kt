@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.utils.Bot

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [configure][Bot.configure], [injection][Bot.injection], [client][Bot.client], [logging][Bot.logging]
 *
 * @param token Your Discord bot token.
 */
@ConfigurationDSL
fun bot(token: String, operate: Bot.() -> Unit) =
    Bot(token, detectGlobalPath(Exception())).apply {
        operate()
        GlobalScope.launch {
            buildBot()
        }
    }

private fun detectGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    val lastIndex = full.lastIndexOf(".").takeIf { it != -1 } ?: full.lastIndex
    return full.substring(0, lastIndex)
}
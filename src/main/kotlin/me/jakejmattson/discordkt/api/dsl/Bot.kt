@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.utils.Bot

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [configure][Bot.configure], [injection][Bot.injection], [logging][Bot.logging]
 *
 * @param token Your Discord bot token.
 */
@ConfigurationDSL
suspend fun bot(token: String, operate: Bot.() -> Unit) {
    val bot = Bot(token, detectGlobalPath(Exception()))

    bot.operate()
    bot.buildBot()
}

private fun detectGlobalPath(exception: Exception): String {
    val full = exception.stackTrace[1].className
    val lastIndex = full.lastIndexOf(".").takeIf { it != -1 } ?: full.lastIndex
    return full.substring(0, lastIndex)
}
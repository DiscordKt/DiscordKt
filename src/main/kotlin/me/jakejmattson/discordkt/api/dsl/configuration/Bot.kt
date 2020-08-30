@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.core.Kord
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.utils.Bot

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [configure][Bot.configure], [injection][Bot.injection], [logging][Bot.logging],
 * [prefix][Bot.prefix], [mentionEmbed][Bot.mentionEmbed], [permissions][Bot.permissions], [presence][Bot.presence]
 *
 * @param token Your Discord bot token.
 */
@ConfigurationDSL
suspend fun bot(token: String, operate: suspend Bot.() -> Unit) {
    val path = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.`package`.name
    val bot = Bot(Kord(token), path)
    bot.operate()
    bot.buildBot()
}
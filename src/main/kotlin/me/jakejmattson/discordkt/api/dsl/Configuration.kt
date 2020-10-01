@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.kordx.emoji.*
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.internal.annotations.ConfigurationDSL
import me.jakejmattson.discordkt.internal.utils.Bot
import java.awt.Color

/**
 * Create an instance of your Discord bot! You can use the following blocks to modify bot configuration:
 * [configure][Bot.configure],
 * [prefix][Bot.prefix],
 * [mentionEmbed][Bot.mentionEmbed],
 * [permissions][Bot.permissions],
 * [presence][Bot.presence]
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

/**
 * Contains all properties configured when the bot is created.
 *
 * @property allowMentionPrefix Allow mentioning the bot to be used as a prefix '@Bot'.
 * @property showStartupLog Whether or not to display log information when the bot starts.
 * @property generateCommandDocs Whether or not command documentation should be generated.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property theme The color theme of internal embeds (i.e. Help).
 */
data class BotConfiguration(
    val allowMentionPrefix: Boolean,
    val showStartupLog: Boolean,
    val generateCommandDocs: Boolean,
    val commandReaction: DiscordEmoji?,
    val theme: Color?,

    internal val prefix: suspend (DiscordContext) -> String,
    internal val mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)?,
    private val permissions: suspend (Command, Discord, User, MessageChannel, Guild?) -> Boolean
) {
    internal suspend fun hasPermission(command: Command, event: CommandEvent<*>) : Boolean {
        return when {
            //TODO check viability
            //command.isDmViable() && event.isFromGuild() -> false
            //command.isGuildViable() && !event.isFromGuild() -> false
            else -> permissions.invoke(command, event.discord, event.author, event.channel, event.guild)
        }
    }
}

/**
 * @suppress Used in sample
 */
data class SimpleConfiguration(var allowMentionPrefix: Boolean = false,
                               var showStartupLog: Boolean = true,
                               var generateCommandDocs: Boolean = true,
                               var commandReaction: DiscordEmoji? = Emojis.eyes,
                               var theme: Color? = null)

/**
 * @suppress Used in sample
 */
data class PermissionContext(val command: Command, val discord: Discord, val user: User, val channel: MessageChannelBehavior, val guild: Guild?)
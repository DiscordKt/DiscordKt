@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.kordx.emoji.*
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.dsl.command.*
import java.awt.Color

/**
 * @property allowMentionPrefix Allow mentioning the bot to be used as a prefix '@Bot'.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property requiresGuild Whether or not commands are required to be executed in a guild.
 */
data class BotConfiguration(
    var allowMentionPrefix: Boolean = false,
    var commandReaction: DiscordEmoji? = Emojis.eyes,
    var requiresGuild: Boolean = true,
    var theme: Color? = null,
    internal var prefix: suspend (DiscordContext) -> String = { "+" },
    internal var mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)? = null,
    internal var permissions: suspend (command: Command, User, MessageChannelBehavior, Guild?) -> Boolean = { _, _, _, _ -> true }
)

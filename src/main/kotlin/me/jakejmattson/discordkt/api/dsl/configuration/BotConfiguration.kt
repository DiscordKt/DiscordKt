@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.kordx.emoji.DiscordEmoji
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.*
import java.awt.Color

/**
 * Contains all properties configured when the bot is created.
 *
 * @property allowMentionPrefix Allow mentioning the bot to be used as a prefix '@Bot'.
 * @property requiresGuild Whether or not commands are required to be executed in a guild.
 * @property showStartupLog Whether or not to display log information when the bot starts.
 * @property generateCommandDocs Whether or not command documentation should be generated.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property theme The color theme of internal embeds (i.e. Help).
 */
data class BotConfiguration(
    //Simple
    val allowMentionPrefix: Boolean,
    val requiresGuild: Boolean,
    val showStartupLog: Boolean,
    val generateCommandDocs: Boolean,
    val commandReaction: DiscordEmoji?,
    val theme: Color?,

    //Predicates
    internal var prefix: suspend (DiscordContext) -> String,
    internal var mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)?,
    internal var permissions: suspend (Command, Discord, User, MessageChannelBehavior, Guild?) -> Boolean
)
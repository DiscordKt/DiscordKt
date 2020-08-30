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
 * @property commandReaction The reaction added to a message when a command is received.
 * @property requiresGuild Whether or not commands are required to be executed in a guild.
 * @property theme The color theme of internal embeds (i.e. Help).
 *
 * @property showStartupLog Whether or not to display log information when the bot starts.
 * @property generateCommandDocs Whether or not command documentation should be generated.
 */
data class BotConfiguration(
    //Simple
    var allowMentionPrefix: Boolean,
    var commandReaction: DiscordEmoji?,
    var requiresGuild: Boolean,
    var theme: Color?,

    //Logging
    val showStartupLog: Boolean,
    val generateCommandDocs: Boolean,

    //Predicates
    internal var prefix: suspend (DiscordContext) -> String,
    internal var mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)?,
    internal var permissions: suspend (Command, Discord, User, MessageChannelBehavior, Guild?) -> Boolean
)
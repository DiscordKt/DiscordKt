@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.gateway.*
import com.gitlab.kordlib.kordx.emoji.*
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.Discord
import java.awt.Color

/**
 * Contains all properties configured when the bot is created.
 *
 * @property allowMentionPrefix Allow mentioning the bot to be used as a prefix '@Bot'.
 * @property showStartupLog Whether or not to display log information when the bot starts.
 * @property generateCommandDocs Whether or not command documentation should be generated.
 * @property recommendCommands Whether or not to recommend the closest command when one fails.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents The registered gateway intents.
 */
data class BotConfiguration(
    val allowMentionPrefix: Boolean,
    val showStartupLog: Boolean,
    val generateCommandDocs: Boolean,
    val recommendCommands: Boolean,
    val commandReaction: DiscordEmoji?,
    val theme: Color?,
    val intents: Set<Intent>,

    internal val prefix: suspend (DiscordContext) -> String,
    internal val mentionEmbed: (suspend EmbedBuilder.(DiscordContext) -> Unit)?,
    private val permissions: suspend (Command, Discord, User, MessageChannel, Guild?) -> Boolean
) {
    internal suspend fun hasPermission(command: Command, event: CommandEvent<*>): Boolean {
        return when {
            command is DmCommand && event.isFromGuild() -> false
            command is GuildCommand && !event.isFromGuild() -> false
            else -> permissions.invoke(command, event.discord, event.author, event.channel, event.guild)
        }
    }
}

/**
 * Holds all basic configuration options.
 *
 * @property allowMentionPrefix Allow mentioning the bot to be used as a prefix '@Bot'.
 * @property showStartupLog Whether or not to display log information when the bot starts.
 * @property generateCommandDocs Whether or not command documentation should be generated.
 * @property recommendCommands Whether or not to recommend the closest command when one fails.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property theme The color theme of internal embeds (i.e. Help).
 * @property intents The registered gateway intents.
 */
data class SimpleConfiguration(var allowMentionPrefix: Boolean = true,
                               var showStartupLog: Boolean = true,
                               var generateCommandDocs: Boolean = true,
                               var recommendCommands: Boolean = true,
                               var commandReaction: DiscordEmoji? = Emojis.eyes,
                               var theme: Color? = null,
                               var intents: Set<Intent> = Intents.nonPrivileged.intents)

/**
 * Holds information used to determine if a command has permission to run.
 *
 * @param command The command invoked that needs to check for permission.
 * @param discord The discord instance.
 * @param user The discord user who invoked the command.
 * @param channel The channel that this command was invoked in.
 * @param guild The guild that this command was invoked in.
 */
data class PermissionContext(val command: Command, val discord: Discord, val user: User, val channel: MessageChannel, val guild: Guild?)
@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.kordx.emoji.*
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.dsl.command.*
import me.jakejmattson.discordkt.internal.annotations.BotConfigurationDSL

/**
 * @property allowMentionPrefix Allow mentioning the bot to be used as a prefix '@Bot'.
 * @property commandReaction The reaction added to a message when a command is received.
 * @property requiresGuild Whether or not commands are required to be executed in a guild.
 */
data class BotConfiguration(
    internal var prefix: (DiscordContext) -> String = { "+" },
    var allowMentionPrefix: Boolean = false,
    var commandReaction: DiscordEmoji? = Emojis.eyes,
    var requiresGuild: Boolean = true,
    internal var mentionEmbed: (EmbedBuilder.(DiscordContext) -> Unit)? = null,
    internal var hasPermission: (command: Command, User, MessageChannelBehavior) -> Boolean = { _, _, _ -> true }
) {
    /**
     * Determine the prefix in a given context.
     */
    @BotConfigurationDSL
    fun prefix(construct: (DiscordContext) -> String) {
        prefix = construct
    }

    /**
     * An embed that will be sent anytime someone (solely) mentions the bot.
     */
    @BotConfigurationDSL
    fun mentionEmbed(construct: EmbedBuilder.(DiscordContext) -> Unit) {
        mentionEmbed = construct
    }

    /**
     * Determine if the given command has permission to be run in this context.
     *
     * @sample PermissionContext
     */
    @BotConfigurationDSL
    fun hasPermission(predicate: (PermissionContext) -> Boolean = { _ -> true }) {
        hasPermission = { command, user, messageChannel ->
            val context = PermissionContext(command, user, messageChannel)
            predicate.invoke(context)
        }
    }

    /**
     * Block to set global color constants, specifically for embeds.
     *
     * @sample ColorConfiguration
     */
    @BotConfigurationDSL
    fun colors(construct: ColorConfiguration.() -> Unit) {
        val colors = ColorConfiguration()
        colors.construct()
    }
}

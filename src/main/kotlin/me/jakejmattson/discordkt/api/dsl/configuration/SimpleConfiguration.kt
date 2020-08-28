package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.kordx.emoji.*
import java.awt.Color

/**
 * @suppress Used in sample
 */
data class Configuration(var allowMentionPrefix: Boolean = false,
                         var commandReaction: DiscordEmoji? = Emojis.eyes,
                         var requiresGuild: Boolean = true,
                         var theme: Color? = null)
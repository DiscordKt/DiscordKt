package me.jakejmattson.discordkt.api.dsl.configuration

import com.gitlab.kordlib.kordx.emoji.*
import java.awt.Color

/**
 * @suppress Used in sample
 */
data class Configuration(var allowMentionPrefix: Boolean = false,
                         var requiresGuild: Boolean = true,
                         var showStartupLog: Boolean = true,
                         var generateCommandDocs: Boolean = true,
                         var commandReaction: DiscordEmoji? = Emojis.eyes,
                         var theme: Color? = null)
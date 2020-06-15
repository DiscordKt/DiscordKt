@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.configuration

import me.jakejmattson.kutils.api.dsl.command.*
import me.jakejmattson.kutils.api.dsl.embed.EmbedDSLHandle
import net.dv8tion.jda.api.entities.*

data class KConfiguration(
    internal var prefix: (DiscordContext) -> String = { "+" },
    var allowMentionPrefix: Boolean = false,
    var commandReaction: String? = "\uD83D\uDC40",
    var deleteErrors: Boolean = false,
    var requiresGuild: Boolean = true,
    internal var mentionEmbed: ((DiscordContext) -> MessageEmbed)? = null,
    internal var visibilityPredicate: (command: Command, User, MessageChannel, Guild?) -> Boolean = { _, _, _, _ -> true }
) {
    fun prefix(construct: (DiscordContext) -> String) {
        prefix = construct
    }

    fun mentionEmbed(construct: EmbedDSLHandle.(DiscordContext) -> Unit) {
        mentionEmbed = {
            val handle = EmbedDSLHandle()
            handle.construct(it)
            handle.build()
        }
    }

    fun visibilityPredicate(predicate: (VisibilityContext) -> Boolean = { _ -> true }) {
        visibilityPredicate = { command, user, messageChannel, guild ->
            val context = VisibilityContext(command, user, messageChannel, guild)
            predicate.invoke(context)
        }
    }

    fun colors(construct: ColorConfiguration.() -> Unit) {
        val colors = ColorConfiguration()
        colors.construct()
        EmbedDSLHandle.successColor = colors.successColor
        EmbedDSLHandle.failureColor = colors.failureColor
        EmbedDSLHandle.infoColor = colors.infoColor
    }
}

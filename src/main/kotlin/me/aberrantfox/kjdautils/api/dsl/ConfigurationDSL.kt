package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.api.dsl.command.Command
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

enum class PrefixDeleteMode {
    Single,
    Double,
    None
}

data class VisibilityContext(val command: Command, val user: User, val channel: MessageChannel, val guild: Guild?)
data class ColorConfiguration(
    var successColor: Color = Color.GREEN,
    var failureColor: Color = Color.RED,
    var infoColor: Color = Color.BLUE
)

data class KConfiguration(
    var prefix: String = "+",
    var allowMentionPrefix: Boolean = false,
    var commandReaction: String? = "\uD83D\uDC40",
    var deleteMode: PrefixDeleteMode = PrefixDeleteMode.None,
    var deleteErrors: Boolean = false,
    var allowPrivateMessages: Boolean = false,
    internal var mentionEmbed: ((GuildMessageReceivedEvent) -> MessageEmbed)? = null,
    internal var visibilityPredicate: (command: Command, User, MessageChannel, Guild?) -> Boolean = { _, _, _, _ -> true }
) {
    fun mentionEmbed(construct: EmbedDSLHandle.(GuildMessageReceivedEvent) -> Unit) {
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

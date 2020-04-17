package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.api.dsl.command.Command
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

enum class PrefixDeleteMode {
    Single,
    Double,
    None
}

data class KConfiguration(
    var prefix: String = "+",
    var reactToCommands: Boolean = true,
    var deleteMode: PrefixDeleteMode = PrefixDeleteMode.Single,
    var deleteErrors: Boolean = false,
    var allowPrivateMessages: Boolean = false,
    var mentionEmbed: ((GuildMessageReceivedEvent) -> MessageEmbed)? = null,
    var visibilityPredicate: (command: Command, User, MessageChannel, Guild?) -> Boolean = { _, _, _, _ -> true }
)

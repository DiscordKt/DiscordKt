package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.api.entities.*

enum class PrefixDeleteMode {
    Single,
    Double,
    None
}

data class KConfiguration(
    var prefix: String = "+",
    var globalPath: String = "",
    var reactToCommands: Boolean = true,
    var deleteMode: PrefixDeleteMode = PrefixDeleteMode.Single,
    var deleteErrors: Boolean = false,
    var allowPrivateMessages: Boolean = false,
    var documentationSortOrder: List<String> = listOf(),
    var mentionEmbed: MessageEmbed? = null,
    var visibilityPredicate: (command: Command, User, MessageChannel, Guild?) -> Boolean = { _, _, _, _ -> true }
)

package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User

enum class PrefixDeleteMode {
    Single,
    Double,
    None
}

data class KConfiguration(val token: String = "",
                          var prefix: String = "+",
                          var globalPath: String = "",
                          var reactToCommands: Boolean = true,
                          var deleteMode: PrefixDeleteMode = PrefixDeleteMode.Single,
                          var visibilityPredicate: (command: String, User, MessageChannel, Guild?) -> Boolean= { _, _, _, _ -> true })

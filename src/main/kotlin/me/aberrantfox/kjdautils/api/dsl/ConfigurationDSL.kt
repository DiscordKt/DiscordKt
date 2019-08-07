package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import java.io.*
import javax.swing.SortOrder

enum class PrefixDeleteMode {
    Single,
    Double,
    None
}

data class KConfiguration(
    val token: String = "",
    var prefix: String = "+",
    var globalPath: String = "",
    var reactToCommands: Boolean = true,
    var deleteMode: PrefixDeleteMode = PrefixDeleteMode.Single,
    var documentationOutputStream: OutputStream = File("commands.md").outputStream(),
    var documentationSortOrder: List<String> = listOf(),
    var visibilityPredicate: (command: String, User, MessageChannel, Guild?) -> Boolean= { _, _, _, _ -> true }
)

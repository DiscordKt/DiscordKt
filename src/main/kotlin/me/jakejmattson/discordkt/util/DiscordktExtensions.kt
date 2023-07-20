package me.jakejmattson.discordkt.util

import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.commands.*

/**
 * Create a discord mention for this command.
 */
public suspend fun Command.mentionOrNull(guild: Guild): String? = when (this) {
    is GuildSlashCommand -> mentionOrNull(guild)
    is GlobalSlashCommand -> mentionOrNull(guild.kord)
    is DmTextCommand -> this.name
    is GlobalTextCommand -> this.name
    is GuildTextCommand -> this.name
}

/**
 * Create a discord mention for this guild slash command.
 */
public suspend fun GuildSlashCommand.mentionOrNull(guild: Guild): String? = guild
    .getApplicationCommands()
    .toList()
    .find { it.name.equals(this.name, true) }
    ?.id
    ?.let { "</${name.lowercase()}:$it>" }

/**
 * Create a discord mention for this global slash command.
 */
public suspend fun GlobalSlashCommand.mentionOrNull(kord: Kord): String? = kord
    .getGlobalApplicationCommands()
    .toList()
    .find { it.name.equals(this.name, true) }
    ?.id
    ?.let { "</${name.lowercase()}:$it>" }
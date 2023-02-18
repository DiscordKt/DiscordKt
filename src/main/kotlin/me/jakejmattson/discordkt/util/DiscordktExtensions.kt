package me.jakejmattson.discordkt.util

import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.commands.GuildSlashCommand

/**
 * Create a discord mention for this guild slash command.
 */
public suspend fun GuildSlashCommand.mention(guild: Guild): String = guild.kord
        .getGuildApplicationCommands(guild.id)
        .toList()
        .find { it.name.equals(this.name, true) }
        ?.id
        ?.let { "</${name.lowercase()}:$it>" }
        ?: "</${name.lowercase()}>"
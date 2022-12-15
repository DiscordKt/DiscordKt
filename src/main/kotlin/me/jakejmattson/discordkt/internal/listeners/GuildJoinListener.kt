package me.jakejmattson.discordkt.internal.listeners

import dev.kord.core.behavior.createApplicationCommands
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.request.KtorRequestException
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.GuildSlashCommand
import me.jakejmattson.discordkt.internal.utils.InternalLogger
import me.jakejmattson.discordkt.util.mapArgs
import me.jakejmattson.discordkt.util.register

internal fun registerGuildJoinListener(discord: Discord) = discord.kord.on<GuildCreateEvent> {
    val guildSlashCommands = discord.commands.filterIsInstance<GuildSlashCommand>()

    if (guildSlashCommands.isEmpty())
        return@on

    try {
        guild.createApplicationCommands {
            guildSlashCommands.forEach {
                register(it)
            }

            discord.subcommands.forEach {
                input(it.name.lowercase(), it.name) {
                    defaultMemberPermissions = it.requiredPermissionLevel

                    it.commands.forEach { command ->
                        subCommand(command.name.lowercase(), command.description.ifBlank { "<No Description>" }) {
                            mapArgs(command)
                        }
                    }
                }
            }
        }
    } catch (e: KtorRequestException) {
        InternalLogger.error("[SLASH] ${Emojis.x.unicode} ${guild.name} - ${e.message}")
    }
}
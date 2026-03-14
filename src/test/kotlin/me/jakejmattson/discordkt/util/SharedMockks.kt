package me.jakejmattson.discordkt.util

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import io.mockk.every
import io.mockk.mockk
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.Command
import me.jakejmattson.discordkt.commands.DiscordContext

fun kord() = mockk<Kord>(relaxed = true) {

}

fun discord(
    _commands: MutableList<Command> = mutableListOf()
) = mockk<Discord> {
    every { commands } returns _commands
}

fun discordContext(
    _discord: Discord = discord(),
    _guild: Guild = guild()
) = mockk<DiscordContext> {
    every { discord } returns _discord
    every { guild } returns _guild
}

fun guild(_id: Snowflake = 0L.toSnowflake()) = mockk<Guild> {
    every { id } returns _id
}
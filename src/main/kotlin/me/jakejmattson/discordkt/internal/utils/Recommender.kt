package me.jakejmattson.discordkt.internal.utils

import dev.kord.common.kColor
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.locale.inject
import java.awt.Color

internal object Recommender {
    private fun recommend(input: String, possibilities: List<String>): String? {
        val (closestMatch, distance) = possibilities
            .map { it to input.levenshteinDistanceTo(it) }
            .minByOrNull { it.second }!!

        return closestMatch.takeUnless { distance > input.length / 2 + 2 }
    }

    suspend fun sendRecommendation(event: CommandEvent<*>, input: String) {
        val discord = event.discord
        val config = discord.configuration

        if (!config.recommendCommands)
            return

        val possibilities = discord.commands
            .filter { it.hasPermissionToRun(event) }
            .flatMap { it.names }
            .takeUnless { it.isEmpty() }
            ?: return

        val recommendation = recommend(input, possibilities) ?: discord.locale.helpName

        event.respond {
            title = discord.locale.unknownCommand
            description = discord.locale.commandRecommendation.inject(recommendation)
            color = Color.RED.kColor
        }
    }
}

private fun String.levenshteinDistanceTo(other: String) = when {
    this == other -> 0
    this == "" -> other.length
    other == "" -> this.length
    else -> (indices).fold((0..other.length).toList()) { previous, u ->
        (other.indices).fold(mutableListOf(u + 1)) { row, v ->
            row.apply {
                add(minOf(
                    row.last() + 1,
                    previous[v + 1] + 1,
                    previous[v] + if (this@levenshteinDistanceTo[u] == other[v]) 0 else 1
                ))
            }
        }
    }.last()
}
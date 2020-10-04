package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.dsl.CommandEvent
import java.awt.Color

internal object Recommender {
    private fun recommend(input: String, possibilities: List<String>): String? {
        val (closestMatch, distance) = possibilities
            .map { it to input.levenshteinDistanceTo(it) }
            .minByOrNull { it.second }!!

        return closestMatch.takeUnless { distance > input.length / 2 + 2 }
    }

    suspend fun sendRecommendation(event: CommandEvent<*>, input: String, possibilities: List<String>) {
        val shouldRecommend = event.discord.configuration.recommendCommands

        if (possibilities.isEmpty() || !shouldRecommend)
            return

        val recommendation = recommend(input, possibilities) ?: "<none>"

        event.respond {
            title = "Unknown Command"
            description = "Recommendation: $recommendation"
            color = Color.RED
        }
    }
}

private fun String.levenshteinDistanceTo(other: String) = when {
    this == other -> 0
    this == "" -> other.length
    other == "" -> this.length
    else -> {
        val initialRow = (0 until other.length + 1).map { it }

        (indices).fold(initialRow, { previous, u ->
            (other.indices).fold(mutableListOf(u + 1), { row, v ->
                row.apply {
                    add(minOf(
                        row.last() + 1,
                        previous[v + 1] + 1,
                        previous[v] + if (this@levenshteinDistanceTo[u] == other[v]) 0 else 1
                    ))
                }
            })
        }).last()
    }
}
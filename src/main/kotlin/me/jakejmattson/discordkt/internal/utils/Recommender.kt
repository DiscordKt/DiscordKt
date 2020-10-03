package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.dsl.CommandEvent
import java.awt.Color

internal object Recommender {
    private fun recommend(input: String, possibilities: List<String>): String? {
        if (possibilities.isEmpty())
            return null

        val (closestMatch, distance) = possibilities
            .map { it to calculateLevenshteinDistance(input, it) }
            .minByOrNull { it.second }!!

        return closestMatch.takeUnless { distance > input.length / 2 + 2 }
    }

    suspend fun sendRecommendation(event: CommandEvent<*>, input: String, possibilities: List<String>) {
        if (!event.discord.configuration.recommendCommands)
            return

        val recommendation = recommend(input, possibilities) ?: "<none>"

        event.respond {
            title = "Unknown Command"
            description = "Recommendation: $recommendation"
            color = Color.RED
        }
    }
}

private fun calculateLevenshteinDistance(left: String, right: String): Int {
    when {
        left == right -> return 0
        left.isEmpty() -> return right.length
        right.isEmpty() -> return left.length
    }

    val v0 = IntArray(right.length + 1) { it }
    val v1 = IntArray(right.length + 1)

    left.indices.forEach { i ->
        v1[0] = i + 1

        left.indices.forEach { j ->
            val cost = if (left[i] == right[j]) 0 else 1
            v1[j + 1] = maxOf(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost)
        }

        v1.copyInto(v0)
    }

    return v1[right.length]
}
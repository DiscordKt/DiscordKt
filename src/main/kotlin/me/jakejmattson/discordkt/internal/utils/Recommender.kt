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
        left.isEmpty() -> right.length
        right.isEmpty() -> left.length
    }

    val v0 = IntArray(right.length + 1) { it }
    val v1 = IntArray(right.length + 1)

    for (i in left.indices) {
        v1[0] = i + 1

        for (j in right.indices) {
            val cost = if (left[i] == right[j]) 0 else 1
            v1[j + 1] = (v1[j] + 1).coerceAtMost((v0[j + 1] + 1).coerceAtMost(v0[j] + cost))
        }

        for (j in 0..right.length)
            v0[j] = v1[j]
    }

    return v1[right.length]
}
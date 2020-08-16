package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.api.dsl.command.*
import java.awt.Color

internal object CommandRecommender {
    private val possibilities = mutableListOf<Command>()

    // only commands that satisfy the predicate will be considered for recommendation
    private fun recommendCommand(input: String, predicate: (Command) -> Boolean): String? {
        val (closestMatch, distance) = possibilities
            .filter(predicate)
            .flatMap { it.names }
            .map { it to calculateLevenshteinDistance(input, it) }
            .minByOrNull { it.second }!!

        return closestMatch.takeUnless { distance > input.length / 2 + 2 }
    }

    fun sendRecommendationEmbed(event: CommandEvent<*>, input: String, predicate: (Command) -> Boolean = { true }) {
        val recommendation = recommendCommand(input, predicate) ?: "<none>"

        event.respond {
            title = "Unknown Command"
            description = "Recommendation: $recommendation"
            color = Color.RED
        }
    }

    fun addAll(list: List<Command>) = possibilities.addAll(list)
}

private fun calculateLevenshteinDistance(left: String, right: String): Int {
    when {
        left == right -> return 0
        left.isEmpty() -> right.length
        right.isEmpty() -> left.length
    }

    val v0 = IntArray(right.length + 1) { it }
    val v1 = IntArray(right.length + 1)

    var cost: Int

    for (i in left.indices) {
        v1[0] = i + 1

        for (j in right.indices) {
            cost = if (left[i] == right[j]) 0 else 1
            v1[j + 1] = (v1[j] + 1).coerceAtMost((v0[j + 1] + 1).coerceAtMost(v0[j] + cost))
        }

        for (j in 0..right.length)
            v0[j] = v1[j]
    }

    return v1[right.length]
}
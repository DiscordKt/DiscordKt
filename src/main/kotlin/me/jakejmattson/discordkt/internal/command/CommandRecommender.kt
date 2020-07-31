package me.jakejmattson.discordkt.internal.command

import me.jakejmattson.discordkt.api.dsl.command.Command
import me.jakejmattson.discordkt.api.dsl.embed.embed
import org.apache.commons.text.similarity.LevenshteinDistance

internal object CommandRecommender {
    private val calc = LevenshteinDistance()
    private val possibilities = mutableListOf<Command>()

    // only commands that satisfy the predicate will be considered for recommendation
    private fun recommendCommand(input: String, predicate: (Command) -> Boolean): String? {
        val (closestMatch, distance) = possibilities.filter(predicate).flatMap { it.names }
            .map { it to calc.apply(input, it) }
            .minBy { it.second }!!

        return closestMatch.takeUnless { distance > input.length / 2 + 2 }
    }

    fun buildRecommendationEmbed(input: String, predicate: (Command) -> Boolean = { true }) =
        embed {
            val recommendation = recommendCommand(input, predicate) ?: "<none>"

            simpleTitle = "Unknown Command"
            description = "Closest Recommendation: $recommendation\n"
            color = failureColor
        }

    fun addAll(list: List<Command>) = possibilities.addAll(list)
}
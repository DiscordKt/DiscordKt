package me.jakejmattson.kutils.internal.command

import me.jakejmattson.kutils.api.dsl.command.Command
import me.jakejmattson.kutils.api.dsl.embed.embed
import org.apache.commons.text.similarity.LevenshteinDistance

object CommandRecommender {
    private val calc = LevenshteinDistance()
    private val possibilities = mutableListOf<Command>()

    // only commands that satisfy the predicate will be considered for recommendation
    fun recommendCommand(input: String, predicate: (Command) -> Boolean = { true }): String? {
        val (closestMatch, distance) = possibilities.filter(predicate).flatMap { it.names }
            .map { it to calc.apply(input, it) }
            .minBy { it.second }!!

        return closestMatch.takeUnless { distance > input.length / 2 + 2 }
    }

    internal fun buildRecommendationEmbed(input: String, predicate: (Command) -> Boolean = { true }) =
        embed {
            val recommendation = recommendCommand(input, predicate) ?: "<none>"

            simpleTitle = "Unknown Command"
            description = "Closest Recommendation: $recommendation\n"
            color = failureColor
        }

    fun addPossibility(item: Command) = possibilities.add(item)

    fun addAll(list: List<Command>) = possibilities.addAll(list)

    fun removePossibility(item: Command) = possibilities.removeAll { it == item }
}
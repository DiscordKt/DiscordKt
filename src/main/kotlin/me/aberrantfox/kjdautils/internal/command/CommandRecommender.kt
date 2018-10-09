package me.aberrantfox.kjdautils.internal.command

import org.apache.commons.text.similarity.LevenshteinDistance

object CommandRecommender {
    private val calc = LevenshteinDistance()
    private val possibilities: MutableList<String> = ArrayList()
    private val smartAssComment = "to spam me like some kind of dummy"

    // only commands that satisfy the predicate will be considered for recommendation
    fun recommendCommand(input: String, predicate: (String) -> Boolean = { true }): String {
        val (reply, distance) = possibilities.filter(predicate)
                .map { it to calc.apply(input, it) }
                .minBy { it.second } ?: return smartAssComment

        return if (distance > input.length / 2 + 2) smartAssComment else reply
    }

    fun addPossibility(item: String) = possibilities.add(item)

    fun addAll(list: List<String>) = possibilities.addAll(list)

    fun removePossibility(item: String) = possibilities.removeAll { it == item.toLowerCase() }
}
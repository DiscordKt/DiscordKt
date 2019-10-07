package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.Command
import org.apache.commons.text.similarity.LevenshteinDistance

object CommandRecommender {
    private val calc = LevenshteinDistance()
    private val possibilities: MutableList<Command> = ArrayList()
    private const val smartAssComment = "to spam me like some kind of dummy"

    // only commands that satisfy the predicate will be considered for recommendation
    fun recommendCommand(input: String, predicate: (Command) -> Boolean = { true }): String {
        val (reply, distance) = possibilities.filter(predicate)
                .map { it.name to calc.apply(input, it.name) }
                .minBy { it.second } ?: return smartAssComment

        return if (distance > input.length / 2 + 2) smartAssComment else reply
    }

    fun addPossibility(item: Command) = possibilities.add(item)

    fun addAll(list: List<Command>) = possibilities.addAll(list)

    fun removePossibility(item: Command) = possibilities.removeAll { it == item }
}
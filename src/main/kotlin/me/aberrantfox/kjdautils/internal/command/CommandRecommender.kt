package me.aberrantfox.kjdautils.internal.command

import org.apache.commons.text.similarity.LevenshteinDistance

object CommandRecommender {
    private val calc = LevenshteinDistance()
    private val possibilities: MutableList<String> = ArrayList()
    private val smartAssComment = "to spam me like some kind of dummy"

    fun recommendCommand(input: String): String {
        val minVal = possibilities.map { calc.apply(input, it) }.min() ?: return smartAssComment

        return if (minVal > (input.length/2) + 3) smartAssComment else possibilities.minBy { calc.apply(input, it) }!!
    }

    fun addPossibility(item: String) = possibilities.add(item)

    fun addAll(list: List<String>) = possibilities.addAll(list)

    fun removePossibility(item: String) = possibilities.removeAll { it == item.toLowerCase() }
}
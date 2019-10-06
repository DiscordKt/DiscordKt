package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.extensions.stdlib.isDigitOrPeriod
import me.aberrantfox.kjdautils.extensions.stdlib.isDouble
import me.aberrantfox.kjdautils.internal.command.ArgumentResult.*
import kotlin.Double

private typealias Quantity = Double
private typealias Quantifier = String

fun convertTimeString(actual: List<String>): ArgumentResult<Double> {

    val possibleEnd = actual.indexOfFirst { toTimeElement(it) == null }

    val timeStringEnd =
            if (possibleEnd != -1) {
                possibleEnd
            } else {
                actual.size
            }

    val original = actual.subList(0, timeStringEnd).toList()

    val possibleElements = original
            .map(String::toLowerCase)
            .map(::toTimeElement)


    val timeElements = possibleElements.dropLastWhile { it is Quantity } // assume trailing numbers are part of next arg (ID, Integer, etc.)

    if (timeElements.isEmpty()) {
        return Error("Invalid time element passed.")
    }

    val consumed = original.subList(0, timeElements.size)

    val quantityCount = timeElements.count { it is Quantity }
    val quantifierCount = timeElements.count { it is Quantifier }

    if (quantityCount != quantifierCount) {
        return Error("The number of quantities doesn't match the number of quantifiers.")
    }


    val hasMissingQuantifier = timeElements.withIndex().any { (index, current) ->
        val next = timeElements.getOrNull(index + 1)

        current is Quantity && next !is Quantifier
    }

    if (hasMissingQuantifier) {
        return Error("At least one quantity is missing a quantifier.")
    }


    val timeInSeconds = timeElements.withIndex()
            .mapNotNull { (index, element) ->
                when (element) {
                    is Pair<*, *> -> element
                    is Quantity -> Pair(element, timeElements[index + 1])
                    else -> null // e.g. quantifiers
                }
            }
            .map { it as Pair<Quantity, Quantifier> }
            .map { it.first * timeStringToSeconds[it.second]!! }
            .reduce { a, b -> a + b }


    return Success(timeInSeconds, consumed)
}

private fun toTimeElement(element: String): Any? {
    val both = toBoth(element)

    if (both != null) return both

    val quantifier = toQuantifier(element)

    if (quantifier != null) return quantifier

    val quantity = toQuantity(element)

    if (quantity != null) return quantity

    return null
}

private fun toQuantifier(element: String) = if (timeStringToSeconds.containsKey(element)) element else null

private fun toQuantity(element: String) = element.toDoubleOrNull()

private fun toBoth(element: String): Pair<Double, String>? {
    val quantity = element.toCharArray().takeWhile { it.isDigitOrPeriod() }.joinToString("")
    val quantifier = element.substring(quantity.length)

    if (!(timeStringToSeconds.containsKey(quantifier))) return null

    if (!(quantity.isDouble())) return null

    return Pair(quantity.toDouble(), quantifier)
}

private val timeStringToSeconds = mapOf(
        "second" to 1,
        "seconds" to 1,
        "s" to 1,
        "sec" to 1,
        "h" to 3600,
        "hour" to 3600,
        "hours" to 3600,
        "minute" to 60,
        "minutes" to 60,
        "m" to 60,
        "mins" to 60,
        "min" to 60,
        "days" to 86400,
        "d" to 86400,
        "day" to 86400,
        "weeks" to 604800,
        "w" to 604800,
        "week" to 604800
)
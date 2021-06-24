package me.jakejmattson.discordkt.internal.utils

import me.jakejmattson.discordkt.api.arguments.ArgumentResult
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.arguments.Success

data class TimePair(val quantity: Double, val quantifier: String) {
    val seconds: Double
        get() = quantity * timeStringToSeconds.getValue(quantifier)
}

internal fun convertTimeString(initial: List<String>): ArgumentResult<Double> {
    val timeStringEnd = initial.indexOfFirst { it.toTimeElement() == null }.takeIf { it != -1 } ?: initial.size
    val original = initial.subList(0, timeStringEnd).toList()
    val possibleElements = original.map { it.lowercase().toTimeElement() }
    val timeElements = possibleElements.dropLastWhile { it is Double } // assume trailing numbers are part of next arg (ID, Integer, etc.)

    if (timeElements.isEmpty())
        return Error("Invalid time element")

    val quantityCount = timeElements.count { it is Double }
    val quantifierCount = timeElements.count { it is String }

    if (quantityCount != quantifierCount)
        return Error("Invalid format")

    val hasMissingQuantifier = timeElements.withIndex().any { (index, current) ->
        val next = timeElements.getOrNull(index + 1)
        current is Double && next !is String
    }

    if (hasMissingQuantifier)
        return Error("Quantity missing quantifier")

    val timePairs = timeElements.mapIndexedNotNull { index, element ->
        when (element) {
            is TimePair -> element
            is Double -> TimePair(element, timeElements[index + 1] as String)
            else -> null
        }
    }

    if (timePairs.any { it.quantity < 0.0 })
        return Error("Cannot be negative")

    val timeInSeconds = timePairs.sumOf { it.seconds }
    return Success(timeInSeconds, original.subList(0, timeElements.size).size)
}

private fun String.toTimeElement() = with(this) { toTimePair() ?: toQuantifier() ?: toQuantity() }
private fun String.toQuantifier() = takeIf { it.lowercase() in timeStringToSeconds }
private fun String.toQuantity() = toDoubleOrNull()

private fun String.toTimePair(): TimePair? {
    val quantityRaw = toCharArray().takeWhile { it.isDigit() || it == '.' }.joinToString("")
    val quantity = quantityRaw.toQuantity() ?: return null
    val quantifier = substring(quantityRaw.length).toQuantifier() ?: return null

    return TimePair(quantity, quantifier)
}

private val timeStringToSeconds = mapOf(
    "s" to 1,
    "sec" to 1,
    "second" to 1,
    "seconds" to 1,

    "m" to 60,
    "min" to 60,
    "mins" to 60,
    "minute" to 60,
    "minutes" to 60,

    "h" to 3600,
    "hr" to 3600,
    "hrs" to 3600,
    "hour" to 3600,
    "hours" to 3600,

    "d" to 86400,
    "day" to 86400,
    "days" to 86400,

    "w" to 604800,
    "week" to 604800,
    "weeks" to 604800,

    "month" to 2592000,
    "months" to 2592000,

    "y" to 31536000,
    "yr" to 31536000,
    "yrs" to 31536000,
    "year" to 31536000,
    "years" to 31536000
)
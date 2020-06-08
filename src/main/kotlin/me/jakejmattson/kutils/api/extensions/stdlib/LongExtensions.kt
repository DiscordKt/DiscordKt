@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.stdlib

import java.time.Duration
import java.time.temporal.ChronoUnit

fun Number.pluralize(unit: String) = "$this ${if (this.toLong() == 1L) unit else "${unit}s"}"

fun Long.toTimeString(trimFront: Boolean = true, trimBack: Boolean = true) = with(Duration.of(this, ChronoUnit.SECONDS)) {
    val timeString =
        "${toDaysPart().pluralize("day")} " +
            "${toHoursPart().pluralize("hour")} " +
            "${toMinutesPart().pluralize("minute")} " +
            toSecondsPart().pluralize("second")

    var grouped = group(timeString)

    if (trimFront)
        grouped = grouped.trimFront()

    if (trimBack)
        grouped = grouped.trimBack()

    assemble(grouped).ifBlank { "0 seconds" }
}

private fun group(timeString: String) = timeString.split(" ").chunked(2).map { it[0] to it[1] }
private fun assemble(timeElements: List<Pair<String, String>>) = timeElements.joinToString(" ") { it.first + " " + it.second }

private fun List<Pair<String, String>>.trimFront() = dropWhile { it.first.toDouble() == 0.0 }
private fun List<Pair<String, String>>.trimBack() = reversed().trimFront().reversed()
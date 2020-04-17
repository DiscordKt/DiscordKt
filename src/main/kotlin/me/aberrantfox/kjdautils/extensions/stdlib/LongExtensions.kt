package me.aberrantfox.kjdautils.extensions.stdlib

import java.time.Duration
import java.time.temporal.*
import java.util.ArrayDeque

private val unitStrings = arrayOf("day", "hour", "minute", "second")

fun Long.toMinimalTimeString(): String {
    val info = arrayOf(0, 0, 0, this@toMinimalTimeString)
    val stack = ArrayDeque<String>()
    fun applyStr(index: Int) =
        stack.push(info[index].pluralize(unitStrings[index]))

    fun evaluate(index: Int, maxValue: Int) =
        with(index + 1) {
            info[index] = info[this] / maxValue
            info[this] = info[this] % maxValue
            applyStr(this)
            info[index] != 0L
        }

    if (evaluate(2, 60) && evaluate(1, 60) && evaluate(0, 24))
        applyStr(0)

    return stack.joinToString(" ")
}

fun Long.toTimeString(unit: TemporalUnit = ChronoUnit.SECONDS) = with(Duration.of(this, unit)) {
    "${toDaysPart().pluralize("day")} " +
        "${toHoursPart().pluralize("hour")}  " +
        "${toMinutesPart().pluralize("minute")} " +
        toSecondsPart().pluralize("second")
}

fun Number.pluralize(unit: String) = "$this ${if (this.toLong() == 1L) unit else "${unit}s"}"
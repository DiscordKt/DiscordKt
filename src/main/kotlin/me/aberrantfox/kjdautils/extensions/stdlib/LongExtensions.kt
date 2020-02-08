package me.aberrantfox.kjdautils.extensions.stdlib

import java.time.Duration
import java.time.temporal.*
import java.util.ArrayDeque

private val unitStrings = arrayOf("day", "hour", "minute", "second")

fun Long.toMinimalTimeString(): String {
    val info = arrayOf(0, 0, 0, this@toMinimalTimeString)
    val stack = ArrayDeque<String>()
    fun applyStr(index: Int) =
        stack.push("${info[index]} ${unitStrings[index]}${if (info[index] == 1L) "" else "s"}")

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

fun Long.convertToTimeString(unit: TemporalUnit = ChronoUnit.SECONDS): String {
    val duration = Duration.of(this, unit)

    fun createChunk(amount: Number, unit: String) = "$amount ${if (amount.toLong() == 1L) unit else "${unit}s"} "

    return with(duration) {
        createChunk(toDaysPart(), "day") +
        createChunk(toHoursPart(), "hour") +
        createChunk(toMinutesPart(), "minute") +
        createChunk(toSecondsPart(), "second").trimEnd()
    }
}
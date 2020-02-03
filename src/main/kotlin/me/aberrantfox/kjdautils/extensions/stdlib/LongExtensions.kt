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

fun main() {
    val longs = arrayOf(0L, 1L, 1000L, 10000000L, 12345789L, 321654987L, 147852369L)
    longs.forEach {
        println(it.toMinimalTimeString())
        println(it.convertToTimeString())
    }
}

fun Long.convertToTimeString(unit: TemporalUnit = ChronoUnit.SECONDS): String {
    val duration = Duration.of(this, unit)

    fun pluralizeIfNeeded(amount: Number, text: String) = "$amount ${text.run { return@run if (amount.toLong() == 1L) text else "${text}s" }}"

    return "${pluralizeIfNeeded(duration.toDaysPart(), "day")} ${pluralizeIfNeeded(duration.toHoursPart(), "hour")} ${pluralizeIfNeeded(duration.toMinutesPart(), "minute")} ${pluralizeIfNeeded(duration.toSecondsPart(), "second")}"
}
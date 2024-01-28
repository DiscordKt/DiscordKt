package me.jakejmattson.discordkt.util

import java.time.Instant

/**
 * Generate discord timestamps.
 */
public object TimeStamp {
    private fun Instant.displayable() = this.toEpochMilli() / 1000

    /**
     * The current time.
     *
     * @param style The [TimeStyle] to output as.
     */
    public fun now(style: TimeStyle = TimeStyle.DATETIME_SHORT): String = "<t:${Instant.now().displayable()}:${style.style}>"

    /**
     * The current time offset by a given numbers of seconds.
     *
     * @param seconds The number of seconds (+/-) to offset by.
     * @param style The [TimeStyle] to output as.
     */
    public fun offsetBy(seconds: Int, style: TimeStyle = TimeStyle.DATETIME_SHORT): String = "<t:${Instant.now().displayable() + seconds}:${style.style}>"

    /**
     * A specific [Instant].
     *
     * @param instant The [Instant] to generate a timestamp for.
     * @param style The [TimeStyle] to output as.
     */
    public fun at(instant: Instant, style: TimeStyle = TimeStyle.DATETIME_SHORT): String = "<t:${instant.displayable()}:${style.style}>"
}

/**
 * Enum for discord [timestamp styles](https://discord.com/developers/docs/reference#message-formatting-timestamp-styles).
 *
 * @param style The discord flag used to determine the timestamp style.
 */
public enum class TimeStyle(public val style: String) {
    /** 16:20 */
    TIME_SHORT("t"),

    /** 16:20:30 */
    TIME_LONG("T"),

    /** 20/04/2021 */
    DATE_SHORT("d"),

    /** 20 April 2021 */
    DATE_LONG("D"),

    /** 20 April 2021 16:20 */
    DATETIME_SHORT("f"),

    /** Tuesday, 20 April 2021 16:20 */
    DATETIME_LONG("F"),

    /** 2 months ago */
    RELATIVE("R")
}
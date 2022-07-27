package arguments

import me.jakejmattson.discordkt.arguments.TimeArg
import utilities.ArgumentTestFactory

private const val second = 1.0
private const val minute = 60.0
private const val hour = 3600.0
private const val day = 86400.0
private const val week = 604800.0
private const val month = 2592000.0
private const val year = 31536000.0

class TimeArgTest : ArgumentTestFactory {
    override val argument = TimeArg

    override val validArgs = listOf(
        "1s" to second,
        "1sec" to second,
        "1second" to second,
        "1seconds" to second,

        "1m" to minute,
        "1min" to minute,
        "1mins" to minute,
        "1minute" to minute,
        "1minutes" to minute,

        "1h" to hour,
        "1hr" to hour,
        "1hrs" to hour,
        "1hour" to hour,
        "1hours" to hour,

        "1d" to day,
        "1day" to day,
        "1days" to day,

        "1w" to week,
        "1week" to week,
        "1weeks" to week,

        "1month" to month,
        "1months" to month,

        "1y" to year,
        "1yr" to year,
        "1yrs" to year,
        "1year" to year,
        "1years" to year,

        "5s" to second * 5,
        "10minutes8seconds" to (10 * minute) + (8 * second),
        "1h2m10seconds" to (hour) + (2 * minute) + (10 * second),
        "1y1w1d1hr1m1s" to year + week + day + hour + minute + second,

        "1SeCoNd" to second,
        "1DAY" to day,
    )

    override val invalidArgs = listOf("5", "-5m", "-5 m", "5n", "hour", "5.5h")
}
package arguments

import me.aberrantfox.kjdautils.internal.arguments.TimeStringArg
import utilities.ArgumentTestFactory

private const val second = 1.0
private const val minute = 60.0
private const val hour = 3600.0
private const val day = 86400.0
private const val week = 604800.0

class TimeStringArgTest : ArgumentTestFactory {
    override val argumentType = TimeStringArg

    override val validArgs = listOf(
        "1s" to second,
        "1 sec" to second,
        "1 second" to second,
        "1 seconds" to second,

        "1m" to minute,
        "1 min" to minute,
        "1 mins" to minute,
        "1 minute" to minute,
        "1 minutes" to minute,

        "1h" to hour,
        "1 hour" to hour,
        "1 hours" to hour,

        "1d" to day,
        "1 day" to day,
        "1 days" to day,

        "1w" to week,
        "1 week" to week,
        "1 weeks" to week,

        "5s" to second * 5,
        "5.5h" to hour * 5.5,
        "10 minutes 8 seconds" to (10 * minute) + (8 * second),
        "1h 2m 10 seconds" to (hour) + (2 * minute) + (10 * second),
        "1w 1d 1h 1m 1s" to week + day + hour + minute + second
    )

    override val invalidArgs = listOf("5", "-5m", "hour", "1m1s", "")
}
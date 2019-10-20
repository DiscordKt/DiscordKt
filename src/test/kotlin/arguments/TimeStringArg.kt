package arguments

import me.aberrantfox.kjdautils.internal.arguments.TimeStringArg
import utilities.SimpleArgTest

private const val second = 1.0
private const val minute = 60.0
private const val hour = 3600.0
private const val day = 86400.0
private const val week = 604800.0

class TimeStringArgTest : SimpleArgTest {
    override val argumentType = TimeStringArg

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
        "1hour" to hour,
        "1hours" to hour,

        "1d" to day,
        "1day" to day,
        "1days" to day,

        "1w" to week,
        "1week" to week,
        "1weeks" to week,

        "5s" to second * 5,
        "5.5h" to hour * 5.5
    )

    override val invalidArgs = listOf("5", "-5m", "hour", "1m 1s", "")
}
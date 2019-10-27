package arguments

import me.aberrantfox.kjdautils.internal.arguments.DoubleArg
import utilities.SimpleArgTest

class DoubleArgTest : SimpleArgTest {
    override val argumentType = DoubleArg

    override val validArgs = listOf(
        "100" to 100.0,
        "-100" to -100.0,
        "1.5" to 1.5,
        "-1.5" to -1.5
    )

    override val invalidArgs = listOf("abcde", "123.a", "-b", "")
}
package arguments

import me.aberrantfox.kjdautils.internal.arguments.IntegerRangeArg
import utilities.ArgumentTestFactory

class IntegerRangeArgTest : ArgumentTestFactory {
    override val argumentType = IntegerRangeArg

    override val validArgs = listOf(
        "0" to 0,
        "10" to 10,
        "5" to 5
    )

    override val invalidArgs = listOf("-1", "11", "5.5", "abcde", "")
}
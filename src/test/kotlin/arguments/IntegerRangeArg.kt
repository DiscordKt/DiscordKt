package arguments

import me.aberrantfox.kutils.api.arguments.IntegerRangeArg
import utilities.ArgumentTestFactory

class IntegerRangeArgTest : ArgumentTestFactory {
    override val argumentType = IntegerRangeArg(0, 10)

    override val validArgs = listOf(
        "0" to 0,
        "10" to 10,
        "5" to 5
    )

    override val invalidArgs = listOf("-1", "11", "5.5", "abcde", "")
}
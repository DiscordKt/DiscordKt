package arguments

import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import utilities.SimpleArgTest

class IntegerArgTest : SimpleArgTest {
    override val argumentType = IntegerArg

    override val validArgs = listOf(
        "100" to 100,
        "-100" to -100,
        "0" to 0
    )

    override val invalidArgs = listOf("abcde", "12.34", "")
}
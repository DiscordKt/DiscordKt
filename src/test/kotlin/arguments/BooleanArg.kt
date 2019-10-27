package arguments

import me.aberrantfox.kjdautils.internal.arguments.BooleanArg
import utilities.SimpleArgTest

class BooleanArgTest : SimpleArgTest {
    override val argumentType = BooleanArg

    override val validArgs = listOf(
        "true" to true,
        "True" to true,
        "false" to false,
        "False" to false
    )

    override val invalidArgs = listOf("abcde", "12345", "")
}

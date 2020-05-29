package arguments

import me.jakejmattson.kutils.api.arguments.BooleanArg
import utilities.ArgumentTestFactory

class BooleanArgTest : ArgumentTestFactory {
    override val argumentType = BooleanArg

    override val validArgs = listOf(
        "true" to true,
        "True" to true,
        "false" to false,
        "False" to false
    )

    override val invalidArgs = listOf("abcde", "12345", "")
}

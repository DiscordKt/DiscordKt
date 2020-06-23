package arguments

import me.jakejmattson.kutils.api.arguments.IntArg
import utilities.ArgumentTestFactory

class IntArgTest : ArgumentTestFactory {
    override val argumentType = IntArg

    override val validArgs = listOf(
        "100" to 100,
        "-100" to -100,
        "0" to 0
    )

    override val invalidArgs = listOf("abcde", "12.34", "")
}
package arguments

import me.jakejmattson.discordkt.api.arguments.DoubleArg
import utilities.ArgumentTestFactory

class DoubleArgTest : ArgumentTestFactory {
    override val argument = DoubleArg

    override val validArgs = listOf(
        "100" to 100.0,
        "-100" to -100.0,
        "1.5" to 1.5,
        "-1.5" to -1.5
    )

    override val invalidArgs = listOf("abcde", "123.a", "-b", "")
}
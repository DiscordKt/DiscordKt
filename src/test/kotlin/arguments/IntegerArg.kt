package arguments

import me.jakejmattson.discordkt.arguments.IntegerArg
import utilities.ArgumentTestFactory

class IntegerArgTest : ArgumentTestFactory {
    override val argument = IntegerArg

    override val validArgs = listOf(
        "100" to 100,
        "-100" to -100,
        "0" to 0
    )

    override val invalidArgs = listOf("abcde", "12.34", "")
}
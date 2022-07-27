package arguments

import me.jakejmattson.discordkt.arguments.CharArg
import utilities.ArgumentTestFactory

class CharArgTest : ArgumentTestFactory {
    override val argument = CharArg

    override val validArgs = listOf(
        "a" to 'a',
        "1" to '1'
    )

    override val invalidArgs = listOf("abc", "")
}

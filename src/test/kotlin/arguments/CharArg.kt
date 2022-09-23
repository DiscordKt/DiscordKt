package arguments

import me.jakejmattson.discordkt.arguments.CharArg
import utilities.StringArgumentTestFactory

class CharArgTest : StringArgumentTestFactory<Char> {
    override val argument = CharArg

    override val validArgs = listOf(
        "a" to 'a',
        "1" to '1'
    )

    override val invalidArgs = listOf("abc", "")
}

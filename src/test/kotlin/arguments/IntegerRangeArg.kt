package arguments

import me.jakejmattson.discordkt.arguments.IntegerRangeArg
import utilities.ArgumentTestFactory

class IntegerRangeArgTest : ArgumentTestFactory<Int, Int> {
    override val argument = IntegerRangeArg(0, 10)

    override val validArgs = listOf(
        0 to 0,
        10 to 10,
        5 to 5
    )

    override val invalidArgs = listOf(-1, 11)
}
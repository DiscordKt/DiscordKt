package arguments

import me.aberrantfox.kjdautils.internal.arguments.CharArg
import utilities.SimpleArgTest

class CharArgTest : SimpleArgTest {
    override val argumentType = CharArg

    override val validArgs = listOf(
        "a" to 'a',
        "1" to '1'
    )

    override val invalidArgs = listOf("abc", "")
}

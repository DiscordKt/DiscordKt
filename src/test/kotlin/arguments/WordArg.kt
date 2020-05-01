package arguments

import me.aberrantfox.kjdautils.internal.arguments.WordArg
import utilities.ArgumentTestFactory

class WordArgTest : ArgumentTestFactory {
    override val argumentType = WordArg

    override val validArgs = listOf(
        "Hello" to "Hello",
        "z" to "z",
        "12345" to "12345",
        "12.45" to "12.45"
    )

    override val invalidArgs = listOf<String>()
}
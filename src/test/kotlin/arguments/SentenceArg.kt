package arguments

import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import utilities.SimpleArgTest

class SentenceArgTest : SimpleArgTest {
    override val argumentType = SentenceArg

    override val validArgs = listOf(
        "HELLO" to "HELLO",
        "world" to "world",
        "hello world" to "hello world"
    )

    override val invalidArgs = listOf<String>()
}
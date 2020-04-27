package arguments

import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import utilities.ArgumentTestFactory

class SentenceArgTest : ArgumentTestFactory {
    override val argumentType = SentenceArg

    override val validArgs = listOf(
        "HELLO" to "HELLO",
        "world" to "world",
        "hello world" to "hello world"
    )

    override val invalidArgs = listOf<String>()
}
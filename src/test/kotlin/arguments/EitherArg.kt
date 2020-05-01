package arguments

import me.aberrantfox.kjdautils.internal.arguments.*
import utilities.ArgumentTestFactory

class EitherArgTest : ArgumentTestFactory {
    override val argumentType = EitherArg(IntegerArg, QuoteArg)

    override val validArgs = listOf(
        "1" to 1,
        "\"A Quote\"" to "A Quote"
    )

    override val invalidArgs = listOf("word")
}
package arguments

import me.jakejmattson.kutils.api.arguments.*
import utilities.ArgumentTestFactory

class EitherArgTest : ArgumentTestFactory {
    override val argumentType = EitherArg(IntegerArg, QuoteArg)

    override val validArgs = listOf(
        "1" to Left(1),
        "\"A Quote\"" to Right("A Quote")
    )

    override val invalidArgs = listOf("word")
}
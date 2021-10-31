package arguments

import me.jakejmattson.discordkt.arguments.*
import utilities.ArgumentTestFactory

class EitherArgTest : ArgumentTestFactory {
    override val argument = EitherArg(IntegerArg, QuoteArg)

    override val validArgs = listOf(
        "1" to Left(1),
        "\"A Quote\"" to Right("A Quote")
    )

    override val invalidArgs = listOf("word")
}
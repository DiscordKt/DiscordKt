package arguments

import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.MultipleArg
import me.jakejmattson.discordkt.arguments.QuoteArg
import utilities.ArgumentTestFactory

class SingleMultipleArgTest : ArgumentTestFactory {
    override val argument = MultipleArg(IntegerArg)

    override val validArgs = listOf(
        "1" to listOf(1),
        "1 2 3" to listOf(1, 2, 3),
        "1 2 a" to listOf(1, 2)
    )

    override val invalidArgs = listOf(
        "a"
    )
}

class MultiMultipleArgTest : ArgumentTestFactory {
    override val argument = MultipleArg(QuoteArg)

    override val validArgs = listOf(
        "\"\"" to listOf(""),
        "\"Hello\"" to listOf("Hello"),
        "\"Hello World\"" to listOf("Hello World"),
        "\"Multiple\" \"quotes\" \"in\" \"one list\"" to listOf("Multiple", "quotes", "in", "one list"),
        "\"Some quotes\" \"and some other\" 1 2 3" to listOf("Some quotes", "and some other")
    )

    override val invalidArgs = listOf(
        "a"
    )
}
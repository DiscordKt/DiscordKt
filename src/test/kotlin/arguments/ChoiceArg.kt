package arguments

import me.jakejmattson.discordkt.arguments.ChoiceArg
import utilities.ArgumentTestFactory

class ChoiceArgTest : ArgumentTestFactory {
    override val argument = ChoiceArg("Choices", "", "a", "b", "c")

    override val validArgs = listOf(
        "a" to "a",
        "B" to "b"
    )

    override val invalidArgs = listOf("abc", "d", "")
}
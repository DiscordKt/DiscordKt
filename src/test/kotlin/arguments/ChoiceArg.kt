package arguments

import me.jakejmattson.discordkt.arguments.ChoiceArg
import utilities.StringArgumentTestFactory

class ChoiceArgTest : StringArgumentTestFactory<String> {
    override val argument = ChoiceArg("Choices", "", "a", "b", "c")

    override val validArgs = listOf(
        "a" to "a",
        "B" to "b"
    )

    override val invalidArgs = listOf("abc", "d", "")
}
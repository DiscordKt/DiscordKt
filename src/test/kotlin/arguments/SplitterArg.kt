package arguments

import me.jakejmattson.discordkt.arguments.SplitterArg
import utilities.ArgumentTestFactory

class SplitterArgTest : ArgumentTestFactory {
    override val argument = SplitterArg

    override val validArgs = listOf(
        "Hello|World" to listOf("Hello", "World"),
        "Hello there|Curious coder" to listOf("Hello there", "Curious coder"),
        "A|B|C" to listOf("A", "B", "C")
    )

    override val invalidArgs = listOf("Hello", "")
}
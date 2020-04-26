package arguments

import me.aberrantfox.kjdautils.internal.arguments.SplitterArg
import utilities.SimpleArgTest

class SplitterArgTest : SimpleArgTest {
    override val argumentType = SplitterArg

    override val validArgs = listOf(
        "Hello|World" to listOf("Hello", "World"),
        "Hello there|Curious coder" to listOf("Hello there", "Curious coder"),
        "a|1|sauce" to listOf("a", "1", "sauce"),
        "Hello" to listOf("Hello"),
        "" to listOf("")
    )

    override val invalidArgs = listOf<String>()
}
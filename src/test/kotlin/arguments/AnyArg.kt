package arguments

import me.jakejmattson.kutils.api.arguments.AnyArg
import utilities.ArgumentTestFactory

class AnyArgTest : ArgumentTestFactory {
    override val argumentType = AnyArg

    override val validArgs = listOf(
        "Hello" to "Hello",
        "z" to "z",
        "12345" to "12345",
        "12.45" to "12.45"
    )

    override val invalidArgs = listOf<String>()
}
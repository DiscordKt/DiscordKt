package arguments

import me.aberrantfox.kjdautils.internal.arguments.*
import utilities.ArgumentTestFactory

class MultipleArgTest() : ArgumentTestFactory {
    override val argumentType = MultipleArg(IntegerArg)

    override val validArgs = listOf(
        "1" to listOf(1),
        "1 2 3" to listOf(1, 2, 3),
        "1 2 a" to listOf(1, 2)
    )

    override val invalidArgs = listOf(
        "a"
    )
}
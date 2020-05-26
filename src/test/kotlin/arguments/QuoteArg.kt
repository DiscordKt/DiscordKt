package arguments

import me.aberrantfox.kutils.api.arguments.QuoteArg
import utilities.ArgumentTestFactory

class QuoteArgTest : ArgumentTestFactory {
    override val argumentType = QuoteArg

    override val validArgs = listOf(
        "\"\"" to "",
        "\"Hello\"" to "Hello",
        "\"Hello World\"" to "Hello World"
    )

    override val invalidArgs = listOf(
        "NoQuotes",
        "No Quotes",
        "\"Leading",
        "Trailing\"",
        "\"Leading Multiple",
        "Trailing Multiple\"",
        "I\"nterrupt\"",
        "\"Interrup\"t"
    )
}
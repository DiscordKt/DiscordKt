package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.*
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class SentenceArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("Hello", "Hello"),
            Arguments.of("world", "world"),
            Arguments.of("Hello World", "Hello World"),
            Arguments.of("12345", "12345"),
            Arguments.of("12.45", "12.45"),
            Arguments.of("a", "a")

            //Fail args
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test SentenceArg conversion function`(arg: String, expected: Any) {
        val argType = SentenceArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
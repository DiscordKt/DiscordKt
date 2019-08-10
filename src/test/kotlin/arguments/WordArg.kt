package arguments

import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class WordArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("Hello", "Hello"),
            Arguments.of("world", "world"),
            Arguments.of("12345", "12345"),
            Arguments.of("12.45", "12.45"),
            Arguments.of("a", "a")

            //Fail args
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test WordArg conversion function`(arg: String, expected: Any) {
        val argType = WordArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
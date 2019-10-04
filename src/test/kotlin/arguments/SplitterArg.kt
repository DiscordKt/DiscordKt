package arguments

import me.aberrantfox.kjdautils.internal.arguments.SplitterArg
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SplitterArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("Hello|World", listOf("Hello", "World")),
            Arguments.of("Hello there|General Kenobi", listOf("Hello there", "General Kenobi")),
            Arguments.of("a|1|sauce", listOf("a", "1", "sauce")),
            Arguments.of("Hello", listOf("Hello")),
            Arguments.of("", listOf(""))
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test SplitterArg conversion function`(arg: String, expected: Any) {
        val argType = SplitterArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Multiple).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
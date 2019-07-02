package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.*
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class OnOffArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            Arguments.of("on", true),
            Arguments.of("On", true),
            Arguments.of("off", false),
            Arguments.of("Off", false),
            Arguments.of("abcde", ArgumentResult.Error),
            Arguments.of("12345", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `test setter configuration commands`(arg: String, expected: Any) {
        val argType = OnOffArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
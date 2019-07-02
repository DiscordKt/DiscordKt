package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.*
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class DoubleArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("100", 100.0),
            Arguments.of("-100", -100.0),
            Arguments.of("1.5", 1.5),
            Arguments.of("${Double.MAX_VALUE}", Double.MAX_VALUE),
            Arguments.of("${Double.MIN_VALUE}", Double.MIN_VALUE),

            //Fail args
            Arguments.of("abcde", ArgumentResult.Error),
            Arguments.of("123.a", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test DoubleArg conversion function`(arg: String, expected: Any) {
        val argType = DoubleArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
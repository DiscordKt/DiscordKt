package arguments

import me.aberrantfox.kjdautils.internal.arguments.YesNoArg
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class YesNoArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("yes", true),
            Arguments.of("Yes", true),
            Arguments.of("no", false),
            Arguments.of("No", false),

            //Fail args
            Arguments.of("abcde", ArgumentResult.Error),
            Arguments.of("12345", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test YesNoArg conversion function`(arg: String, expected: Any) {
        val argType = YesNoArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
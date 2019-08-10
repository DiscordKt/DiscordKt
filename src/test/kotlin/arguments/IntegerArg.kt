package arguments

import me.aberrantfox.kjdautils.internal.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class IntegerArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("100", 100),
            Arguments.of("-100", -100),
            Arguments.of("${Integer.MAX_VALUE}", Integer.MAX_VALUE),
            Arguments.of("${Integer.MIN_VALUE}", Integer.MIN_VALUE),

            //Fail args
            Arguments.of("abcde", ArgumentResult.Error),
            Arguments.of("12.34", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test IntegerArg conversion function`(arg: String, expected: Any) {
        val argType = IntegerArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
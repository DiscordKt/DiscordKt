package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.*
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class IntegerRangeArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("0", 0),
            Arguments.of("10", 10),
            Arguments.of("5", 5),

            //Fail args
            Arguments.of("-1", ArgumentResult.Error),
            Arguments.of("11", ArgumentResult.Error),
            Arguments.of("5.5", ArgumentResult.Error),
            Arguments.of("abcde", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test IntegerRangeArg conversion function`(arg: String, expected: Any) {
        val argType = IntegerRangeArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
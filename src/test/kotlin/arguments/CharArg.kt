package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.*
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class CharArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("a", 'a'),
            Arguments.of("1", '1'),

            //Fail args
            Arguments.of("abc", ArgumentResult.Error),
            Arguments.of("Hello World", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test CharArg conversion function`(arg: String, expected: Any) {
        val argType = CharArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
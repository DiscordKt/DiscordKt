package arguments

import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.command.arguments.BooleanArg
import mock.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class BooleanArgTest {
    companion object {
        @JvmStatic
        fun arguments() = listOf(
            Arguments.of("T", true),
            Arguments.of("true", true),
            Arguments.of("True", true),
            Arguments.of("F", false),
            Arguments.of("false", false),
            Arguments.of("False", false),
            Arguments.of("abcde", ArgumentResult.Error),
            Arguments.of("12345", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `test setter configuration commands`(arg: String, expected: Any) {
        val argType = BooleanArg.attemptConvert(arg)

        if (argType is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (argType as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
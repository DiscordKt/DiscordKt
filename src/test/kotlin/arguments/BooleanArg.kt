package arguments

import me.aberrantfox.kjdautils.internal.arguments.BooleanArg
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import mock.attemptConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class BooleanArgTest {
    companion object {
        private val modifiedArg = BooleanArg(name = "On or Off", truthValue = "On", falseValue = "Off")

        @JvmStatic
        fun arguments() = listOf(
            //Pass args
            Arguments.of("true", true),
            Arguments.of("True", true),
            Arguments.of("false", false),
            Arguments.of("False", false),

            //Fail args
            Arguments.of("abcde", ArgumentResult.Error),
            Arguments.of("12345", ArgumentResult.Error),
            Arguments.of("", ArgumentResult.Error)
        )

        @JvmStatic
        fun modifiedArguments() = listOf(
            //Pass args
            Arguments.of(modifiedArg.truthValue, true),
            Arguments.of(modifiedArg.falseValue, false),

            //Fail args
            Arguments.of(modifiedArg.truthValue + modifiedArg.falseValue, ArgumentResult.Error)
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Test standard BooleanArg conversion function`(arg: String, expected: Any) {
        val result = BooleanArg.attemptConvert(arg)

        if (result is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (result as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }

    @ParameterizedTest
    @MethodSource("modifiedArguments")
    fun `Test modified BooleanArg conversion function`(arg: String, expected: Any) {
        val result = modifiedArg.attemptConvert(arg)

        if (result is ArgumentResult.Error) {
            Assertions.assertEquals(ArgumentResult.Error, expected)
            return
        }

        val convertedValue = (result as ArgumentResult.Single).result
        Assertions.assertEquals(convertedValue, expected)
    }
}
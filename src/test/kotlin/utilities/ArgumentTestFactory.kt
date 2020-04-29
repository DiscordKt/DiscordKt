package utilities

import me.aberrantfox.kjdautils.internal.command.*
import mock.commandEventMock
import org.junit.jupiter.api.*

interface ArgumentTestFactory {
    val argumentType: ArgumentType<*>
    val validArgs: List<Pair<String, *>>
    val invalidArgs: List<String>

    @TestFactory
    fun `valid input`() = validArgs.map { (input, expected) ->
        when (val conversionResult = argumentType.attemptConvert(input)) {
            is ArgumentResult.Success -> {
                DynamicTest.dynamicTest("\"$input\" -> $expected") {
                    Assertions.assertTrue(true)
                }
            }
            is ArgumentResult.Error -> {
                DynamicTest.dynamicTest("\"$input\" -> ${conversionResult.error}") {
                    fail { conversionResult.error }
                }
            }
        }
    }

    @TestFactory
    fun `invalid input`() = invalidArgs.map { input ->
        when (val conversionResult = argumentType.attemptConvert(input)) {
            is ArgumentResult.Error -> {
                DynamicTest.dynamicTest("\"$input\" -> ${conversionResult.error}") {
                    Assertions.assertTrue(true)
                }
            }
            is ArgumentResult.Success -> {
                DynamicTest.dynamicTest("\"$input\" -> ${conversionResult.result}") {
                    fail { "Conversion succeeded, but was expected to fail." }
                }
            }
        }
    }
}

private fun ArgumentType<*>.attemptConvert(input: String): ArgumentResult<*> {
    val split = input.split(" ")
    return convert(split.first(), split, commandEventMock)
}
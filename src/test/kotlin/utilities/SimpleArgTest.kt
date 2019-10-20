package utilities

import me.aberrantfox.kjdautils.internal.command.*
import org.junit.jupiter.api.*

interface SimpleArgTest {
    val argumentType: ArgumentType<*>
    val validArgs: List<Pair<String, *>>
    val invalidArgs: List<String>

    @TestFactory
    fun `valid input`() = validArgs.map { (input, expected) ->
        DynamicTest.dynamicTest("\"$input\" -> $expected") {
            Assertions.assertEquals(expected, argumentType.convertToSingle(input))
        }
    }

    @TestFactory
    fun `invalid input`() = invalidArgs.map { input ->
        DynamicTest.dynamicTest("\"$input\" -> Conversion Error") {
            Assertions.assertTrue(argumentType.attemptConvert(input) is ArgumentResult.Error)
        }
    }
}
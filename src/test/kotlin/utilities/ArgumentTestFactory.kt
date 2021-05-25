package utilities

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import org.junit.jupiter.api.*

private val commandEventMock = mockk<CommandEvent<*>> { }

interface ArgumentTestFactory {
    val argumentType: ArgumentType<*>
    val validArgs: List<Pair<String, *>>
    val invalidArgs: List<String>

    @TestFactory
    fun `valid input`() = validArgs.map { (input, expected) ->
        when (val conversionResult = argumentType.attemptConvert(input)) {
            is Success<*> -> {
                DynamicTest.dynamicTest("\"$input\" -> $expected") {
                    Assertions.assertEquals(conversionResult.result, expected)
                }
            }
            is Error<*> -> {
                DynamicTest.dynamicTest("\"$input\" -> ${conversionResult.error}") {
                    fail { conversionResult.error }
                }
            }
        }
    }

    @TestFactory
    fun `invalid input`() = invalidArgs.map { input ->
        when (val conversionResult = argumentType.attemptConvert(input)) {
            is Error<*> -> {
                DynamicTest.dynamicTest("\"$input\" -> ${conversionResult.error}") {
                    Assertions.assertTrue(true)
                }
            }
            is Success<*> -> {
                DynamicTest.dynamicTest("\"$input\" -> ${conversionResult.result}") {
                    fail { "Conversion succeeded, but was expected to fail." }
                }
            }
        }
    }
}

private fun ArgumentType<*>.attemptConvert(input: String): ArgumentResult<*> {
    val split = input.split(" ")
    var result: ArgumentResult<*>

    runBlocking {
        result = convert(split.first(), split, commandEventMock)
    }

    return result
}
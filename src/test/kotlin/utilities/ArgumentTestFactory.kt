package utilities

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.locale.LocaleEN
import org.junit.jupiter.api.*

private val commandEventMock = mockk<CommandEvent<*>> { }

interface ArgumentTestFactory {
    val argument: Argument<*>
    val validArgs: List<Pair<String, *>>
    val invalidArgs: List<String>

    @TestFactory
    fun `valid input`() = validArgs.map { (input, expected) ->
        when (val conversionResult = argument.attemptConvert(input)) {
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
        when (val conversionResult = argument.attemptConvert(input)) {
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

    companion object {
        @BeforeAll
        @JvmStatic
        fun initLocale() {
            internalLocale = LocaleEN()
        }
    }
}

private fun Argument<*>.attemptConvert(input: String): ArgumentResult<*> {
    val split = input.split(" ")
    var result: ArgumentResult<*>

    runBlocking {
        result = convert(split.first(), split, commandEventMock)
    }

    return result
}
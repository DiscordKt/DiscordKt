package utilities

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.LocaleEN
import org.junit.jupiter.api.*

private val discordMockk = mockk<Discord>()

private val contextMock = mockk<DiscordContext> {
    every { discord } returns discordMockk
}

interface ArgumentTestFactory <A, B> {
    val argument: Argument<A, B>
    val validArgs: List<Pair<A, B>>
    val invalidArgs: List<A>

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

interface StringArgumentTestFactory<O> : ArgumentTestFactory<String, O>

private fun <A, B> Argument<A, B>.attemptConvert(input: A): Result<*> = runBlocking { transform(input, contextMock) }
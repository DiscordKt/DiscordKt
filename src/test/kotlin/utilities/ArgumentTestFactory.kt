package utilities

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.internal.utils.simplerName

private val discordMockk = mockk<Discord>()

private val contextMock = mockk<DiscordContext> {
    every { discord } returns discordMockk
}

fun <A, B> DescribeSpec.generatePassTests(arg: Argument<A, B>, inputs: List<Pair<A, B>>) {
    describe(arg::class.simplerName) {
        inputs.forEach { (input, expected) ->
            val result = io.kotest.common.runBlocking { arg.transform(input, contextMock) }

            it("$input -> $expected") {
                result.shouldBeTypeOf<Success<*>>()
                result.result.shouldBe(expected)
            }
        }
    }
}

fun <A, B> DescribeSpec.generateFailTests(arg: Argument<A, B>, inputs: List<A>) {
    describe(arg::class.simplerName) {
        inputs.forEach { input ->
            val result = io.kotest.common.runBlocking { arg.transform(input, contextMock) }

            it("Fail: '$input'") {
                result.shouldBeTypeOf<Error<*>>()
            }
        }
    }
}
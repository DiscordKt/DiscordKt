package util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.scopes.DescribeSpecContainerScope
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
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

class ArgTestBuilder<A, B>(private val arg: Argument<A, B>, private val spec: DescribeSpecContainerScope) {
    suspend infix fun A.becomes(expected: B) {
        val result = io.kotest.common.runBlocking { arg.transform(this, contextMock) }

        spec.it("$this -> $result") {
            result.shouldBeTypeOf<Success<*>>()
            result.result.shouldBe(expected)
        }
    }
}

fun <A, B> DescribeSpec.generateFailTests(arg: Argument<A, B>, inputs: List<A>) {
    describe(arg::class.simplerName) {
        inputs.forEach { input ->
            val result = runBlocking { arg.transform(input, contextMock) }

            it("Fail: '$input'") {
                result.shouldBeTypeOf<Error<*>>()
            }
        }
    }
}
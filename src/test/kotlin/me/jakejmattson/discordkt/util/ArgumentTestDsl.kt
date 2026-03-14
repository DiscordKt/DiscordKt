package me.jakejmattson.discordkt.util

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.scopes.DescribeSpecContainerScope
import io.kotest.matchers.shouldBe
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.commands.DiscordContext

@ArgumentTestDsl
suspend fun <A, B> DescribeSpecContainerScope.transform(
    arg: Argument<A, B>,
    discordContext: DiscordContext = discordContext(),
    builder: suspend TransformTestBuilder<A, B>.() -> Unit
) {
    builder.invoke(TransformTestBuilder(arg, this, discordContext))
}

@ArgumentTestDsl
suspend fun <A, B> DescribeSpecContainerScope.parse(
    arg: Argument<A, B>,
    builder: suspend ParseTestBuilder<A, B>.() -> Unit
) {
    builder.invoke(ParseTestBuilder(arg, this))
}

class TransformTestBuilder<A, B>(
    private val arg: Argument<A, B>,
    private val spec: DescribeSpecContainerScope,
    private val context: DiscordContext
) {
    @ArgumentTestDsl
    suspend infix fun A.becomes(expected: B) {
        val result = arg.transform(this, context)

        spec.it("$this -> ${stringify(expected)}") {
            result.shouldBeRight(expected)
        }
    }

    @ArgumentTestDsl
    suspend infix fun A.producesError(expected: String) {
        val result = arg.transform(this, discordContext())

        spec.it("$this -> $expected") {
            result.shouldBeLeft(expected)
        }
    }
}

class ParseTestBuilder<A, B>(private val arg: Argument<A, B>, private val spec: DescribeSpecContainerScope) {
    @ArgumentTestDsl
    suspend infix fun String.becomes(expected: B?) {
        val result = arg.parse(mutableListOf(this), discord())

        spec.it("$this -> $expected") {
            result.shouldBe(expected)
        }
    }

    @ArgumentTestDsl
    suspend infix fun List<String>.becomes(expected: B?) {
        val result = arg.parse(this.toMutableList(), discord())

        spec.it("$this -> $expected") {
            result.shouldBe(expected)
        }
    }
}

@DslMarker
annotation class ArgumentTestDsl
package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.parse

class EveryArgTest : DescribeSpec({
    val arg = EveryArg

    describe("When the input is valid") {
        parse(arg) {
            "HELLO" becomes "HELLO"
            "world" becomes "world"
            listOf("hello", "world") becomes "hello world"
        }
    }
})

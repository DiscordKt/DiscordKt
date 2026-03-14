package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.parse

class DoubleArgTest : DescribeSpec({
    describe("when the input is valid") {
        parse(DoubleArg) {
            "12.34" becomes 12.34
            "5" becomes 5.0
        }
    }

    describe("when the input is not valid") {
        parse(DoubleArg) {
            "abc" becomes null
            "1.2.3" becomes null
        }
    }
})
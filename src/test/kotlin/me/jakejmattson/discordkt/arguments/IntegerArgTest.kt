package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.parse

class IntegerArgTest : DescribeSpec({
    describe("when the input is valid") {
        parse(IntegerArg) {
            "6" becomes 6
            "12345" becomes 12345
            "-7" becomes -7
        }
    }

    describe("when the input is not valid") {
        parse(IntegerArg) {
            "abc" becomes null
            "12.34" becomes null
            "5.0" becomes null
        }
    }
})

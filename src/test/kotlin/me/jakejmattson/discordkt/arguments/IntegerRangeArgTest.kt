package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.transform

class IntegerRangeArgTest : DescribeSpec({
    val arg = IntegerRangeArg(0, 10)

    describe("When the input is valid") {
        transform(arg) {
            0 becomes 0
            10 becomes 10
            5 becomes 5
        }
    }

    describe("When the input is invalid") {
        transform(arg) {
            (-1) producesError "Not in range 0-10"
            11 producesError "Not in range 0-10"
        }
    }
})

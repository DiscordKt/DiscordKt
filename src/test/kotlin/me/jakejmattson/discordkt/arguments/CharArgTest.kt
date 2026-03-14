package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.transform

class CharArgTest : DescribeSpec({
    val arg = CharArg

    describe("When the input is valid") {
        transform(arg) {
            "a" becomes 'a'
            "1" becomes '1'
        }
    }

    describe("When the input is invalid") {
        transform(arg) {
            "abc" producesError "Must be a single character"
            "" producesError "Must be a single character"
        }
    }
})

package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.transform

class ChoiceArgTest : DescribeSpec({
    val arg = ChoiceArg("Choices", "", "a", "b", "c")

    describe("When the input is valid") {
        transform(arg) {
            "a" becomes "a"
            "B" becomes "b"
        }
    }

    describe("When the input is invalid") {
        transform(arg) {
            "abc" producesError "Invalid selection"
            "d" producesError "Invalid selection"
            "" producesError "Invalid selection"
        }
    }
})

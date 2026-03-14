package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.parse
import me.jakejmattson.discordkt.util.transform

class MultipleArgTest : DescribeSpec({
    describe("when the input is valid") {
        parse(IntegerArg.multiple()) {
            listOf("1", "2", "3") becomes listOf(1, 2, 3)
            listOf("1", "2", "a", "b", "3") becomes listOf(1, 2)
        }

        transform(CharArg.multiple()) {
            listOf("a", "b") becomes listOf('a', 'b')
        }
    }

    describe("when the input is not valid") {
        parse(IntegerArg.multiple()) {
            listOf("a") becomes null
            listOf("a", "1") becomes null
        }

        transform(CharArg.multiple()) {
            listOf("a", "bcd") producesError "Must be a single character"
        }
    }
})

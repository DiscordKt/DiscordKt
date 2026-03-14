package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.transform

class OptionalArgTest : DescribeSpec({
    describe("when the input is valid") {
        transform(CharArg.optional('J')) {
            "a" becomes 'a'
            "9" becomes '9'
        }
    }

    describe("when the input is not valid") {
        transform(CharArg.optional('J')) {
            "Hello" becomes 'J'
        }

        transform(CharArg.optionalNullable(null)) {
            "Hello" becomes null
        }
    }
})

package me.jakejmattson.discordkt.arguments

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.parse

class BooleanArgTest : DescribeSpec({
    describe("default implementation") {
        describe("when the input is valid") {
            parse(BooleanArg) {
                "true" becomes true
                "false" becomes false
                "TRUE" becomes true
                "FALSE" becomes false
            }
        }

        describe("when the input is not valid") {
            parse(BooleanArg) {
                "truefalse" becomes null
                "something" becomes null
            }
        }
    }

    describe("custom implementation") {
        val customArg = BooleanArg("YnArg", "yes", "no")

        describe("when the input is valid") {
            parse(customArg) {
                "yes" becomes true
                "no" becomes false
            }
        }

        describe("when the input is not valid") {
            parse(customArg) {
                "true" becomes null
                "false" becomes null
            }
        }
    }

    describe("invalid implementation") {
        describe("when the truth value is blank") {
            it("should throw an exception") {
                shouldThrow<IllegalArgumentException> {
                    BooleanArg(truthValue = "")
                }
            }
        }

        describe("when the false value is blank") {
            it("should throw an exception") {
                shouldThrow<IllegalArgumentException> {
                    BooleanArg(falseValue = "")
                }
            }
        }

        describe("when the true and false values are the same") {
            it("should throw an exception") {
                shouldThrow<IllegalArgumentException> {
                    BooleanArg(truthValue = "hi", falseValue = "HI")
                }
            }
        }
    }
})

package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.parse

class QuoteArgTest : DescribeSpec({
    describe("when the input is valid") {
        parse(QuoteArg) {
            listOf("\"quote\"") becomes "quote"
            listOf("\"multiple", "word", "quote\"") becomes "multiple word quote"
            listOf("\"multiple", "word", "quote\"", "with stuff after") becomes "multiple word quote"
        }
    }

    describe("when the input is not valid") {
        parse(QuoteArg) {
            "when missing \"starting quote\"".split(" ") becomes null
            "\"when missing ending quote".split(" ") becomes null
        }
    }
})

package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.util.transform

class SplitterArgTest : DescribeSpec({
    val arg = SplitterArg

    describe("When the input is valid") {
        transform(arg) {
            "Hello|World" becomes listOf("Hello", "World")
            "Hello there|Curious coder" becomes listOf("Hello there", "Curious coder")
            "A|B|C" becomes listOf("A", "B", "C")
        }
    }
})

package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.transform
import java.awt.Color

class HexColorArgTest : DescribeSpec({
    val arg = HexColorArg

    describe("When the input is valid") {
        transform(arg) {
            "#000000" becomes Color(0x000000)
            "#FFFFFF" becomes Color(0xFFFFFF)
            "#00bFfF" becomes Color(0x00BFFF)
            "000000" becomes Color(0x000000)
            "FFFFFF" becomes Color(0xFFFFFF)
        }
    }

    describe("When the input is invalid") {
        transform(arg) {
            "black" producesError internalLocale.invalidFormat
            "0" producesError internalLocale.invalidFormat
            "#gggggg" producesError internalLocale.invalidFormat
            "FFFFFFFF" producesError internalLocale.invalidFormat
        }
    }
})

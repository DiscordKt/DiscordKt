package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.transform

class UrlArgTest : DescribeSpec({
    describe("when the input is valid") {
        transform(UrlArg) {
            "https://github.com/DiscordKt/DiscordKt" becomes "https://github.com/DiscordKt/DiscordKt"
            "www.jakejmattson.me" becomes "www.jakejmattson.me"
        }
    }

    describe("when the input is invalid") {
        transform(UrlArg) {
            ":::" producesError internalLocale.invalidFormat
            "abc" producesError internalLocale.invalidFormat
        }
    }
})
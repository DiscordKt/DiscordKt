package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.LocaleEN
import me.jakejmattson.discordkt.util.transform

class AnyArgTest : DescribeSpec({
    // Every other test gets this set by PackageConfig. No idea why this one won't work
    internalLocale = LocaleEN()

    val arg = AnyArg

    describe("When the input is valid") {
        transform(arg) {
            "z" becomes "z"
            "Hello" becomes "Hello"
            "12345" becomes "12345"
            "12.45" becomes "12.45"
        }
    }
})
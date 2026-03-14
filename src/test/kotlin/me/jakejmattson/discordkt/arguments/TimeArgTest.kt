package me.jakejmattson.discordkt.arguments

import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.*

class TimeArgTest : DescribeSpec({
    val arg = TimeArg

    describe("When the input is valid") {
        transform(arg) {
            "1second" becomes second
            "1minute" becomes minute
            "1hour" becomes hour
            "1day" becomes day
            "1week" becomes week
            "1month" becomes month

            "1y" becomes year
            "1yr" becomes year
            "1yrs" becomes year
            "1year" becomes year
            "1years" becomes year

            "5s" becomes second * 5
            "10minutes8seconds" becomes (10 * minute) + (8 * second)
            "1h2m10seconds" becomes (hour) + (2 * minute) + (10 * second)
            "1y1w1d1hr1m1s" becomes year + week + day + hour + minute + second
            "1y 1w 1d 1hr 1m 1s" becomes year + week + day + hour + minute + second
            "1 m i n u t e" becomes minute

            "1SeCoNd" becomes second
            "1DAY" becomes day
        }
    }

    describe("When the input is invalid") {
        transform(arg) {
            "5" producesError internalLocale.invalidFormat
            "-5m" producesError internalLocale.invalidFormat
            "-5 m" producesError internalLocale.invalidFormat
            "hour" producesError internalLocale.invalidFormat
            "5.5h" producesError internalLocale.invalidFormat

            "5n" producesError "Unknown quantifier: n"
        }
    }
})

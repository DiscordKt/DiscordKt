import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.internal.utils.simplerName
import me.jakejmattson.discordkt.locale.LocaleEN
import util.*
import java.awt.Color

class ArgumentPass : DescribeSpec({
    internalLocale = LocaleEN()

    fun <A, B> Argument<A, B>.testFor(builder: suspend ArgTestBuilder<A, B>.() -> Unit) {
        describe(this::class.simplerName) {
            val argTester = ArgTestBuilder(this@testFor, this)
            builder.invoke(argTester)
        }
    }

    AnyArg.testFor {
        "z" becomes "z"
        "Hello" becomes "Hello"
        "12345" becomes "12345"
        "12.45" becomes "12.45"
    }

    CharArg.testFor {
        "a" becomes 'a'
        "1" becomes '1'
    }

    ChoiceArg("Choices", "", "a", "b", "c").testFor {
        "a" becomes "a"
        "B" becomes "b"
    }

    EveryArg.testFor {
        "HELLO" becomes "HELLO"
        "world" becomes "world"
        "hello world" becomes "hello world"
    }

    HexColorArg.testFor {
        "#000000" becomes Color(0x000000)
        "#FFFFFF" becomes Color(0xFFFFFF)
        "#00bFfF" becomes Color(0x00BFFF)
        "000000" becomes Color(0x000000)
        "FFFFFF" becomes Color(0xFFFFFF)
    }

    IntegerRangeArg(0, 10).testFor {
        0 becomes 0
        10 becomes 10
        5 becomes 5
    }

    SplitterArg.testFor {
        "Hello|World" becomes listOf("Hello", "World")
        "Hello there|Curious coder" becomes listOf("Hello there", "Curious coder")
        "A|B|C" becomes listOf("A", "B", "C")
    }

    TimeArg.testFor {
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
})
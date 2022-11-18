import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.LocaleEN
import util.*
import java.awt.Color

class ValidInput : DescribeSpec({
    internalLocale = LocaleEN()

    infix fun <A, B> Argument<A, B>.passesWith(inputs: List<Pair<A, B>>) = generatePassTests(this, inputs)

    AnyArg passesWith listOf(
        "Hello" to "Hello",
        "z" to "z",
        "12345" to "12345",
        "12.45" to "12.45"
    )

    CharArg passesWith listOf(
        "a" to 'a',
        "1" to '1'
    )

    ChoiceArg("Choices", "", "a", "b", "c") passesWith listOf(
        "a" to "a",
        "B" to "b"
    )

    EveryArg passesWith listOf(
        "HELLO" to "HELLO",
        "world" to "world",
        "hello world" to "hello world"
    )

    HexColorArg passesWith listOf(
        "#000000" to Color(0x000000),
        "000000" to Color(0x000000),
        "#FFFFFF" to Color(0xFFFFFF),
        "FFFFFF" to Color(0xFFFFFF),
        "#00bFfF" to Color(0x00BFFF)
    )

    IntegerRangeArg(0, 10) passesWith listOf(
        0 to 0,
        10 to 10,
        5 to 5
    )

    SplitterArg passesWith listOf(
        "Hello|World" to listOf("Hello", "World"),
        "Hello there|Curious coder" to listOf("Hello there", "Curious coder"),
        "A|B|C" to listOf("A", "B", "C")
    )

    TimeArg passesWith listOf(
        "1second" to second,
        "1minute" to minute,
        "1hour" to hour,
        "1day" to day,
        "1week" to week,
        "1month" to month,

        "1y" to year,
        "1yr" to year,
        "1yrs" to year,
        "1year" to year,
        "1years" to year,

        "5s" to second * 5,
        "10minutes8seconds" to (10 * minute) + (8 * second),
        "1h2m10seconds" to (hour) + (2 * minute) + (10 * second),
        "1y1w1d1hr1m1s" to year + week + day + hour + minute + second,
        "1y 1w 1d 1hr 1m 1s" to year + week + day + hour + minute + second,
        "1 m i n u t e" to minute,

        "1SeCoNd" to second,
        "1DAY" to day,
    )
})
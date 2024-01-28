import io.kotest.core.spec.style.DescribeSpec
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.LocaleEN
import util.generateFailTests

class ArgumentFail : DescribeSpec({
    internalLocale = LocaleEN()

    infix fun <A, B> Argument<A, B>.failsWith(inputs: List<A>) = generateFailTests(this, inputs)

    CharArg failsWith listOf("abc", "")

    ChoiceArg("Choices", "", "a", "b", "c") failsWith listOf("abc", "d", "")

    HexColorArg failsWith listOf("black", "0", "#gggggg", "FFFFFFFF")

    IntegerRangeArg(0, 10) failsWith listOf(-1, 11)

    TimeArg failsWith listOf("5", "-5m", "-5 m", "5n", "hour", "5.5h")
})
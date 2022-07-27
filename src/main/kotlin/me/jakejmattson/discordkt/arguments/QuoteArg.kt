package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a group of arguments surrounded by quotation marks.
 */
public open class QuoteArg(override val name: String = "Quote",
                           override val description: String = internalLocale.quoteArgDescription) : StringArgument<String> {
    /**
     * Accepts a group of arguments surrounded by quotation marks.
     */
    public companion object : QuoteArg()

    //https://unicode-table.com/en/sets/quotation-marks/
    private val quotationMarks = listOf(
        '"', //Double universal
        '\u201C', //English double left
        '\u201D', //English double right
        '\u2018', //English single left
        '\u2019', //English single right
    )

    override suspend fun parse(args: MutableList<String>, discord: Discord): String? {
        val arg = args.first()
        val first = arg.first()
        val last = arg.last()

        if (first !in quotationMarks)
            return null

        val rawQuote = if (last !in quotationMarks)
            args.takeUntil { it.last() !in quotationMarks }.joinToString(" ")
        else
            arg

        if (rawQuote.last() !in quotationMarks)
            return null

        val quote = rawQuote.trim(*quotationMarks.toCharArray())

        args.removeAll(args.filterIndexed { index: Int, _: String ->
            index < quote.split(" ").size
        })

        return quote
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("\"A Quote\"")
}

private fun List<String>.takeUntil(predicate: (String) -> Boolean): List<String> {
    val result = this.takeWhile(predicate).toMutableList()
    val index = result.size

    if (index in indices)
        result.add(this[index])

    return result
}
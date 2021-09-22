package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale

/**
 * Accepts a group of arguments surrounded by quotation marks.
 */
public open class QuoteArg(override val name: String = "Quote",
                    override val description: String = internalLocale.quoteArgDescription) : Argument<String> {
    /**
     * Accepts a group of arguments surrounded by quotation marks.
     */
    public companion object : QuoteArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        //https://unicode-table.com/en/sets/quotation-marks/
        val quotationMarks = listOf(
            '"', //Double universal
            '\u201C', //English double left
            '\u201D', //English double right
            '\u2018', //English single left
            '\u2019', //English single right
        )
        val first = arg.first()
        val last = arg.last()

        if (first !in quotationMarks)
            return Error("No opening quotation mark")

        val rawQuote = if (last !in quotationMarks)
            args.takeUntil { it.last() !in quotationMarks }.joinToString(" ")
        else
            arg

        if (rawQuote.last() !in quotationMarks)
            return Error("No closing quotation mark")

        val quote = rawQuote.trim(*quotationMarks.toCharArray())
        val consumedCount = quote.split(" ").size

        return Success(quote, consumedCount)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("\"A Quote\"")
    override fun formatData(data: String): String = "\"$data\""
}

private fun List<String>.takeUntil(predicate: (String) -> Boolean): List<String> {
    val result = this.takeWhile(predicate).toMutableList()
    val index = result.size

    if (index in indices)
        result.add(this[index])

    return result
}
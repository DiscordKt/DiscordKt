package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class QuoteArg(override val name: String = "Quote") : ArgumentType<String>() {
    companion object : QuoteArg()

    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {

        val quotationMark = '"'

        if (!arg.startsWith(quotationMark)) {
            return ArgumentResult.Error("Expected an opening quotation mark, found: $arg")
        }

        val rawQuote = if (arg.endsWith(quotationMark)) {
            arg
        } else {
            args.takeUntil { !it.endsWith(quotationMark) }.joinToString(" ")
        }

        if (!rawQuote.endsWith(quotationMark)) {
            return ArgumentResult.Error("Missing closing quotation mark.")
        }

        val quote = rawQuote.trim(quotationMark)
        val consumedCount = quote.split(" ").size
        val consumed = args.take(consumedCount)

        return ArgumentResult.Success(quote, consumed)
    }

    override fun generateExamples(event: CommandEvent<*>) = mutableListOf("\"A Quote\"")
}

private fun List<String>.takeUntil(predicate: (String) -> Boolean): List<String> {
    val result = this.takeWhile(predicate).toMutableList()
    val index = result.size

    if (index in indices)
        result.add(this[index])

    return result
}
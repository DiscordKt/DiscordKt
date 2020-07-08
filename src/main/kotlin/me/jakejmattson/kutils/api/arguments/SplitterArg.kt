package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * Consumes all arguments and returns a list of the results (split by splitter character).
 *
 * @param splitter The character used to split the input.
 */
open class SplitterArg(override val name: String = "TextWithSplitter", private val splitter: String = "|") : ArgumentType<List<String>>() {
    companion object : SplitterArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<String>> {
        val joined = args.joinToString(" ")

        if (!joined.contains(splitter))
            return Error("$name requires the character `$splitter` to split input.")

        return Success(joined.split(splitter).toList(), args.size)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("A${splitter}B${splitter}C")
}
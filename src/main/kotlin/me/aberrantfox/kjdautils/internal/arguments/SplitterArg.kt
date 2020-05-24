package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class SplitterArg(override val name: String = "TextWithSplitter", private val splitter: String = "|") : ArgumentType<List<String>>() {
    companion object : SplitterArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<String>> {
        val joined = args.joinToString(" ")

        if (!joined.contains(splitter))
            return ArgumentResult.Error("Input does not contain the splitter character: `$splitter`")

        return ArgumentResult.Success(joined.split(splitter).toList(), args.size)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("A${splitter}B${splitter}C")
}
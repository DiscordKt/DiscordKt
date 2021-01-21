package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent

/**
 * Consumes all arguments and returns a list of the results (split by splitter character).
 *
 * @param splitter The character used to split the input.
 */
open class SplitterArg(override val name: String = "TextWithSplitter", private val splitter: String = "|") : ArgumentType<List<String>> {
    /**
     * Consumes all arguments and returns a list of the results (split by '|' character).
     */
    companion object : SplitterArg()

    override val description = "Items split by $splitter"

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<String>> {
        val joined = args.joinToString(" ")

        if (!joined.contains(splitter))
            return Error("Missing '$splitter'")

        return Success(joined.split(splitter).toList(), args.size)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf("A${splitter}B${splitter}C")
    override fun formatData(data: List<String>) = data.joinToString(splitter)
}
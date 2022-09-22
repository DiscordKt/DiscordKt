package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * Consumes all arguments and returns a list of the results (split by splitter character).
 *
 * @param splitter The character used to split the input.
 */
public open class SplitterArg(private val splitter: String = "|",
                              override val name: String = "TextWithSplitter",
                              override val description: String = internalLocale.splitterArgDescription.inject(splitter)) : StringArgument<List<String>> {
    /**
     * Consumes all arguments and returns a list of the results (split by splitter character).
     */
    public companion object : SplitterArg()

    override suspend fun transform(input: String, context: DiscordContext): Result<List<String>> {
        return Success(input.split(splitter).toList())
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("A${splitter}B${splitter}C")
}
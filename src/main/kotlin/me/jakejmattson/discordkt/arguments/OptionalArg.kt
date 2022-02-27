package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * An optional argument with a default value.
 */
public class OptionalArg<I, O, O2>(override val name: String,
                                        override val base: Argument<I, O>,
                                        internal val default: suspend DiscordContext.() -> O2) : WrappedArgument<I, O, I, O2> {
    override val description: String = internalLocale.optionalArgDescription.inject(base.name)

    override suspend fun parse(args: MutableList<String>, discord: Discord): I? {
        return base.parse(args, discord)
    }

    override suspend fun transform(input: I, context: DiscordContext): Result<O2> {
        val transformation = base.transform(input, context)

        return if (transformation is Success)
            transformation as Result<O2>
        else
            Success(default.invoke(context))
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = base.generateExamples(context)
}
package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * An optional argument with a default value.
 */
public class OptionalArg<Input, Output>(override val name: String,
                                        internal val type: Argument<Input, Output>,
                                        internal val default: suspend DiscordContext.() -> Output) : SpecialArgument<Input, Output> {
    override val description: String = internalLocale.optionalArgDescription.inject(type.name)

    override suspend fun parse(args: MutableList<String>, discord: Discord): Input? {
        return type.parse(args, discord)
    }

    override suspend fun transform(input: Input, context: DiscordContext): ArgumentResult<Output> {
        val transformation = type.transform(input, context)

        return if (transformation is Success)
            transformation
        else
            Success(default.invoke(context))
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = type.generateExamples(context)
}
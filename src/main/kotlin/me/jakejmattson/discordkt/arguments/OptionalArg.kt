package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * An optional argument with a default value.
 */
public class OptionalArg<I, O, O2>(
    override val name: String,
    override val type: Argument<I, O>,
    internal val default: suspend DiscordContext.() -> O2
) : WrappedArgument<I, O, I, O2> {
    override val description: String = internalLocale.optionalArgDescription.inject(type.name)

    override suspend fun transform(input: I, context: DiscordContext): Either<String, O2> = either {
        val transformation = type.transform(input, context)

        transformation.getOrElse { default.invoke(context) } as O2
    }
}
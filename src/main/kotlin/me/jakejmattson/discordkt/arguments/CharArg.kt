package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a single character.
 */
public open class CharArg(
    override val name: String = "Character",
    override val description: String = internalLocale.charArgDescription
) : StringArgument<Char> {
    /**
     * Accepts a single character.
     */
    public companion object : CharArg()

    override suspend fun transform(input: String, context: DiscordContext): Either<String, Char> = either {
        ensure(input.length == 1) {
            "Must be a single character"
        }

        input[0]
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = ('a'..'z').map { it.toString() }
}
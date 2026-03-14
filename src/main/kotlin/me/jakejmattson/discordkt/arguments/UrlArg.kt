package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.containsURl

/**
 * Accepts a string that matches the URL regex.
 */
public open class UrlArg(
    override val name: String = "URL",
    override val description: String = internalLocale.urlArgDescription
) : StringArgument<String> {
    /**
     * Accepts a string that matches the URL regex.
     */
    public companion object : UrlArg()

    override suspend fun transform(input: String, context: DiscordContext): Either<String, String> = either {
        ensure(input.containsURl()) {
            internalLocale.invalidFormat
        }

        input
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("https://www.google.com")
}
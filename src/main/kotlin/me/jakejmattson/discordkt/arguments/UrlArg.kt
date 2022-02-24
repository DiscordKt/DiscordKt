package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.extensions.containsURl

/**
 * Accepts a string that matches the URL regex.
 */
public open class UrlArg(override val name: String = "URL",
                         override val description: String = internalLocale.urlArgDescription) : StringArgument<String> {
    /**
     * Accepts a string that matches the URL regex.
     */
    public companion object : UrlArg()

    override suspend fun transform(input: String, context: DiscordContext): ArgumentResult<String> {
        return if (input.containsURl())
            Success(input)
        else
            Error(internalLocale.invalidFormat)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("https://www.google.com")
}
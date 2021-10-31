package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.extensions.containsURl

/**
 * Accepts a string that matches the URL regex.
 */
public open class UrlArg(override val name: String = "URL",
                         override val description: String = internalLocale.urlArgDescription) : Argument<String> {
    /**
     * Accepts a string that matches the URL regex.
     */
    public companion object : UrlArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        return if (arg.containsURl())
            Success(arg)
        else
            Error(internalLocale.invalidFormat)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("https://www.google.com")
}
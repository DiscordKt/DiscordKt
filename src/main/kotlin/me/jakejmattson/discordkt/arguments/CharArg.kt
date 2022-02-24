package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a single character.
 */
public open class CharArg(override val name: String = "Character",
                          override val description: String = internalLocale.charArgDescription) : StringArgument<Char> {
    /**
     * Accepts a single character.
     */
    public companion object : CharArg()

    override suspend fun transform(input: String, context: DiscordContext): ArgumentResult<Char> {
        return if (input.length == 1)
            Success(input[0])
        else
            Error("Must be a single character")
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = ('a'..'z').map { it.toString() }
}
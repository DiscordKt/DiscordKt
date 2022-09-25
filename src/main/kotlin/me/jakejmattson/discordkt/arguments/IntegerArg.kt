package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accept a whole number in the int range.
 */
public open class IntegerArg(override val name: String = "Integer",
                             override val description: String = internalLocale.integerArgDescription) : IntegerArgument<Int> {
    /**
     * Accept a whole number in the int range.
     */
    public companion object : IntegerArg()

    override suspend fun transform(input: Int, context: DiscordContext): Result<Int> = Success(input)
}
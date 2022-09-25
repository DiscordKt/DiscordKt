package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accept a decimal number in the double range.
 */
public open class DoubleArg(override val name: String = "Double",
                            override val description: String = internalLocale.doubleArgDescription) : DoubleArgument<Double> {
    /**
     * Accept a decimal number in the double range.
     */
    public companion object : DoubleArg()

    override suspend fun transform(input: Double, context: DiscordContext): Result<Double> = Success(input)
}
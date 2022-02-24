package me.jakejmattson.discordkt.arguments

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
}
package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * Accepts either of two values. Defaults to true/false.
 *
 * @param truthValue The string value that results in true.
 * @param falseValue The string value that results in false.
 */
public open class BooleanArg(override val name: String = "Boolean",
                             final override val truthValue: String = "true",
                             final override val falseValue: String = "false",
                             override val description: String = internalLocale.booleanArgDescription.inject(truthValue, falseValue)) : BooleanArgument<Boolean> {
    /**
     * Accepts either true or false.
     */
    public companion object : BooleanArg()

    init {
        require(truthValue.isNotEmpty() && falseValue.isNotEmpty()) { "Custom BooleanArg ($name) options cannot be empty!" }
        require(!truthValue.equals(falseValue, ignoreCase = true)) { "Custom BooleanArg ($name) options cannot be the same!" }
    }

    override suspend fun transform(input: Boolean, context: DiscordContext): Result<Boolean> = Success(input)
}
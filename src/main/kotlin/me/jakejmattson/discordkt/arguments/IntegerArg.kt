package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accept a whole number in the int range.
 */
public open class IntegerArg(override val name: String = "Integer",
                             override val description: String = internalLocale.integerArgDescription) : Argument<Int> {
    /**
     * Accept a whole number in the int range.
     */
    public companion object : IntegerArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> =
        when (val result = arg.toIntOrNull()) {
            null -> Error(internalLocale.invalidFormat)
            else -> Success(result)
        }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = (0..10).map { it.toString() }
}
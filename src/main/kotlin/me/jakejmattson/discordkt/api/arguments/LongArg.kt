package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale

/**
 * Accept a whole number in the long range.
 */
public open class LongArg(override val name: String = "Long",
                   override val description: String = internalLocale.longArgDescription) : Argument<Long> {
    /**
     * Accept a whole number in the long range.
     */
    public companion object : LongArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Long> =
        when (val result = arg.toLongOrNull()) {
            null -> Error(internalLocale.invalidFormat)
            else -> Success(result)
        }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = (0..10).map { it.toString() }
}
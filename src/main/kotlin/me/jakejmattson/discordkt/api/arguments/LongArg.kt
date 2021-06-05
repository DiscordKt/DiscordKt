package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale

/**
 * Accept a whole number in the long range.
 */
open class LongArg(override val name: String = "Long") : ArgumentType<Long> {
    /**
     * Accept a whole number in the long range.
     */
    companion object : LongArg()

    override val description = internalLocale.longArgDescription

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toLongOrNull()) {
            null -> Error(internalLocale.invalidFormat)
            else -> Success(result)
        }

    override suspend fun generateExamples(event: CommandEvent<*>) = (0..10).map { it.toString() }
}
package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*

/**
 * Accept a whole number in the long range.
 */
open class LongArg(override val name: String = "Long") : ArgumentType<Long>() {
    /**
     * Accept a whole number in the long range.
     */
    companion object : LongArg()

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>) =
        when (val result = arg.toLongOrNull()) {
            null -> Error("Invalid format")
            else -> Success(result)
        }

    override fun generateExamples(event: GlobalCommandEvent<*>) = (0..10).map { it.toString() }
}
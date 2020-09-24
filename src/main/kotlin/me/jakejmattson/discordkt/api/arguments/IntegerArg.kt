package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*

/**
 * Accept a whole number in the int range.
 */
open class IntegerArg(override val name: String = "Integer") : ArgumentType<Int>() {
    /**
     * Accept a whole number in the int range.
     */
    companion object : IntegerArg()

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>) =
        when (val result = arg.toIntOrNull()) {
            null -> Error("Invalid format")
            else -> Success(result)
        }

    override fun generateExamples(event: GlobalCommandEvent<*>) = (0..10).map { it.toString() }
}
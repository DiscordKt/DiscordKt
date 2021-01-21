package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent

/**
 * Accept a whole number in the int range.
 */
open class IntegerArg(override val name: String = "Integer") : ArgumentType<Int> {
    /**
     * Accept a whole number in the int range.
     */
    companion object : IntegerArg()

    override val description = "A whole number"

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toIntOrNull()) {
            null -> Error("Invalid format")
            else -> Success(result)
        }

    override suspend fun generateExamples(event: CommandEvent<*>) = (0..10).map { it.toString() }
}
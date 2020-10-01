package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*

/**
 * Accepts a single character.
 */
open class CharArg(override val name: String = "Character") : ArgumentType<Char>() {
    /**
     * Accepts a single character.
     */
    companion object : CharArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Char> {
        return if (arg.length == 1)
            Success(arg[0])
        else
            Error("Must be a single character")
    }

    override fun generateExamples(event: CommandEvent<*>) = ('a'..'z').map { it.toString() }
}

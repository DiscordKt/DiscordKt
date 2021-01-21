package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent

/**
 * Accepts any (single) argument. Does not accept empty strings.
 */
open class AnyArg(override val name: String = "Any") : ArgumentType<String> {
    /**
     * Accepts any (single) argument. Does not accept empty strings.
     */
    companion object : AnyArg()

    override val description = "A single word or value"

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> =
        if (arg.isNotEmpty()) Success(arg) else Error("Cannot be empty")

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf(name)
}
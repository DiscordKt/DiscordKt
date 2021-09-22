package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale

/**
 * Accepts any (single) argument. Does not accept empty strings.
 */
public open class AnyArg(override val name: String = "Any",
                         override val description: String = internalLocale.anyArgDescription) : Argument<String> {
    /**
     * Accepts any (single) argument. Does not accept empty strings.
     */
    public companion object : AnyArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> =
        if (arg.isNotEmpty()) Success(arg) else Error("Cannot be empty")

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf(name)
}
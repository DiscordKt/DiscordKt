package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Consumes all remaining arguments. Does not accept empty strings.
 */
public open class EveryArg(override val name: String = "Text",
                           override val description: String = internalLocale.everyArgDescription) : Argument<String> {
    /**
     * Consumes all remaining arguments. Does not accept empty strings.
     */
    public companion object : EveryArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        if (args.size in 0..1 && arg.isEmpty())
            return Error("Cannot be empty")

        return Success(args.joinToString(" "), args.size)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("This is a sample sentence.")
}
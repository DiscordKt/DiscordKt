package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Consumes all remaining arguments. Does not accept empty strings.
 */
public open class EveryArg(override val name: String = "Text",
                           override val description: String = internalLocale.everyArgDescription) : StringArgument<String> {
    /**
     * Consumes all remaining arguments. Does not accept empty strings.
     */
    public companion object : EveryArg()

    override suspend fun parse(args: MutableList<String>, discord: Discord): String? {
        if (args.size in 0..1 && args.first().isEmpty())
            return null

        return args.joinToString(" ").also { args.clear() }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("This is a sample sentence.")
}
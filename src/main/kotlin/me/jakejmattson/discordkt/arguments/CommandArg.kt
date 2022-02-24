package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.Command
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.commands.get
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a DiscordKt command by name.
 */
public open class CommandArg(override val name: String = "Command",
                             override val description: String = internalLocale.commandArgDescription) : StringArgument<Command> {
    /**
     * Accepts a DiscordKt command by name.
     */
    public companion object : CommandArg()

    override suspend fun transform(input: String, context: DiscordContext): ArgumentResult<Command> {
        val command = context.discord.commands[input] ?: return Error(internalLocale.notFound)
        return Success(command)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = context.discord.commands.mapNotNull { it.names.firstOrNull() }
    override fun formatData(data: Command): String = data.names.first()
}
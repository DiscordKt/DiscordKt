package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import me.jakejmattson.discordkt.commands.Command
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.commands.findByName
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a DiscordKt command by name.
 */
public open class CommandArg(
    override val name: String = "Command",
    override val description: String = internalLocale.commandArgDescription
) : StringArgument<Command> {
    /**
     * Accepts a DiscordKt command by name.
     */
    public companion object : CommandArg()

    override suspend fun transform(input: String, context: DiscordContext): Either<String, Command> = either {
        ensureNotNull(context.discord.commands.findByName(input)) {
            internalLocale.notFound
        }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> =
        context.discord.commands.mapNotNull { it.names.firstOrNull() }
}
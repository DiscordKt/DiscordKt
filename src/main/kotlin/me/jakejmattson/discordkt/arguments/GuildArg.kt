package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.firstOrNull
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.toSnowflakeOrNull

/**
 * Accepts a Discord Guild entity as an ID.
 */
public open class GuildArg(
    override val name: String = "Guild",
    override val description: String = internalLocale.guildArgDescription
) : StringArgument<Guild> {
    /**
     * Accepts a Discord Guild entity as an ID.
     */
    public companion object : GuildArg()

    override suspend fun transform(input: String, context: DiscordContext): Either<String, Guild> = either {
        ensureNotNull(context.discord.kord.guilds.firstOrNull { it.id == input.toSnowflakeOrNull() }) {
            internalLocale.notFound
        }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> =
        context.guild?.let { listOf(it.id.toString()) }
            ?: listOf()
}
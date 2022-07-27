package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.firstOrNull
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord Guild entity as an ID.
 */
public open class GuildArg(override val name: String = "Guild",
                           override val description: String = internalLocale.guildArgDescription) : StringArgument<Guild> {
    /**
     * Accepts a Discord Guild entity as an ID.
     */
    public companion object : GuildArg()

    override suspend fun transform(input: String, context: DiscordContext): Result<Guild> {
        val guild = context.discord.kord.guilds.firstOrNull { it.id == input.toSnowflakeOrNull() }
            ?: return Error(internalLocale.notFound)

        return Success(guild)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = context.guild?.let { listOf(it.id.toString()) }
        ?: listOf()
}
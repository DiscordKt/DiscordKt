package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Guild
import dev.kord.core.firstOrNull
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord Guild entity as an ID.
 */
public open class GuildArg(override val name: String = "Guild",
                           override val description: String = internalLocale.guildArgDescription) : Argument<Guild> {
    /**
     * Accepts a Discord Guild entity as an ID.
     */
    public companion object : GuildArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Guild> {
        val guild = event.discord.kord.guilds.firstOrNull { it.id == arg.toSnowflakeOrNull() }
            ?: return Error(internalLocale.notFound)

        return Success(guild)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = event.guild?.let { listOf(it.id.asString) }
        ?: listOf()

    override fun formatData(data: Guild): String = data.name
}
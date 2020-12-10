package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.firstOrNull
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord Guild entity as an ID.
 */
open class GuildArg(override val name: String = "Guild") : ArgumentType<Guild> {
    /**
     * Accepts a Discord Guild entity as an ID.
     */
    companion object : GuildArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Guild> {
        val guild = event.discord.api.guilds.firstOrNull { it.id == arg.toSnowflakeOrNull() }
            ?: return Error("Not found")

        return Success(guild)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = event.guild?.let { listOf(it.id.asString) } ?: listOf()
    override fun formatData(data: Guild) = data.name
}

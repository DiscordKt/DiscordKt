package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.firstOrNull
import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.stdlib.trimToSnowflake

/**
 * Accepts a Discord Guild entity as an ID.
 */
open class GuildArg(override val name: String = "Guild") : ArgumentType<Guild>() {
    /**
     * Accepts a Discord Guild entity as an ID.
     */
    companion object : GuildArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Guild> {
        val guild = event.discord.kord.guilds.firstOrNull { it.id == arg.trimToSnowflake() }
            ?: return Error("Not found")

        return Success(guild)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.guild?.id.toString())
    override fun formatData(data: Guild) = data.name
}

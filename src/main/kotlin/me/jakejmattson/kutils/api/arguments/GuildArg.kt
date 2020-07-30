package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.Guild

/**
 * Accepts a Discord Guild entity as an ID.
 */
open class GuildArg(override val name: String = "Guild") : ArgumentType<Guild>() {
    /**
     * Accepts a Discord Guild entity as an ID.
     */
    companion object : GuildArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Guild> {
        val guild = event.discord.jda.guilds.firstOrNull { it.id == arg.trimToID() }
            ?: return Error("Not found")

        return Success(guild)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.guild?.id ?: "244230771232079873")
    override fun formatData(data: Guild) = data.name
}

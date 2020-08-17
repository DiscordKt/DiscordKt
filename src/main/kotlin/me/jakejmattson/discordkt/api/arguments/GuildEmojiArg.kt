package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.GuildEmoji
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.stdlib.*

/**
 * Accepts a guild emote.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class GuildEmojiArg(override val name: String = "Guild Emote", private val allowsGlobal: Boolean = false) : ArgumentType<GuildEmoji>() {
    /**
     * Accepts a guild emote from within this guild.
     */
    companion object : GuildEmojiArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<GuildEmoji> {
        val trimmed = arg.trimToID()
        val split = trimmed.split(":")

        val id = when (split.size) {
            1 -> split[0]
            3 -> split[2]
            else -> return Error("Not found")
        }.toSnowflake()

        val availableEmotes =
            if (allowsGlobal)
                event.discord.kord.guilds.toList().flatMap { it.emojis }
            else
                event.guild?.emojis ?: emptyList()

        val emote = availableEmotes.firstOrNull { it.id == id } ?: return Error("Not found")

        return Success(emote)
    }

    override fun generateExamples(event: CommandEvent<*>) = event.guild?.emojis?.map { it.mention } ?: emptyList()
}
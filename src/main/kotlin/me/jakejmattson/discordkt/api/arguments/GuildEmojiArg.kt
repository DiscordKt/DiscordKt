package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.core.entity.GuildEmoji
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.*

/**
 * Accepts a guild emoji.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class GuildEmojiArg(override val name: String = "Guild Emoji", private val allowsGlobal: Boolean = false) : ArgumentType<GuildEmoji>() {
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

        val availableEmojis =
            if (allowsGlobal)
                event.discord.api.guilds.toList().flatMap { it.emojis }
            else
                event.guild?.emojis ?: emptyList()

        val emoji = availableEmojis.firstOrNull { it.id == id } ?: return Error("Not found")

        return Success(emoji)
    }

    override fun generateExamples(event: CommandEvent<*>) = event.guild?.emojis?.map { it.mention } ?: emptyList()
}
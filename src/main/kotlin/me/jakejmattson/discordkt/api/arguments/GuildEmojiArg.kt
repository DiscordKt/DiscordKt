package me.jakejmattson.discordkt.api.arguments

import dev.kord.core.entity.GuildEmoji
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.jakejmattson.discordkt.api.extensions.trimToID

/**
 * Accepts a guild emoji.
 *
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class GuildEmojiArg(override val name: String = "Guild Emoji",
                         override val description: String = internalLocale.guildEmojiArgDescription,
                         private val allowsGlobal: Boolean = false) : Argument<GuildEmoji> {
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
            else -> return Error(internalLocale.notFound)
        }.toSnowflakeOrNull()

        val availableEmojis =
            if (allowsGlobal)
                event.discord.kord.guilds.toList().flatMap { it.emojis.toList() }
            else
                event.guild?.emojis?.toList() ?: emptyList()

        val emoji = availableEmojis.firstOrNull { it.id == id } ?: return Error(internalLocale.notFound)

        return Success(emoji)
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = event.guild?.emojis?.toList()?.map { it.mention }
        ?: emptyList()
}
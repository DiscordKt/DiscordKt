package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.GuildEmoji
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.util.toSnowflakeOrNull
import me.jakejmattson.discordkt.util.trimToID

/**
 * Accepts a guild emoji.
 *
 * @param allowsGlobal Whether this entity can be retrieved from outside this guild.
 */
public open class GuildEmojiArg(override val name: String = "Guild Emoji",
                                override val description: String = internalLocale.guildEmojiArgDescription,
                                private val allowsGlobal: Boolean = false) : StringArgument<GuildEmoji> {
    /**
     * Accepts a guild emote from within this guild.
     */
    public companion object : GuildEmojiArg()

    override suspend fun transform(input: String, context: DiscordContext): Result<GuildEmoji> {
        val trimmed = input.trimToID()
        val split = trimmed.split(":")

        val id = when (split.size) {
            1 -> split[0]
            3 -> split[2]
            else -> return Error(internalLocale.notFound)
        }.toSnowflakeOrNull()

        val availableEmojis =
            if (allowsGlobal)
                context.discord.kord.guilds.toList().flatMap { it.emojis.toList() }
            else
                context.guild?.emojis?.toList() ?: emptyList()

        val emoji = availableEmojis.firstOrNull { it.id == id } ?: return Error(internalLocale.notFound)

        return Success(emoji)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = context.guild?.emojis?.toList()?.map { it.mention }
        ?: emptyList()
}
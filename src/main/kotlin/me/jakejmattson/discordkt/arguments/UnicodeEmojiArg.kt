package me.jakejmattson.discordkt.arguments

import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a unicode emoji.
 */
public open class UnicodeEmojiArg(override val name: String = "Emoji",
                                  override val description: String = internalLocale.unicodeEmojiArgDescription) : StringArgument<DiscordEmoji> {
    /**
     * Accepts a unicode emoji.
     */
    public companion object : UnicodeEmojiArg()

    override suspend fun transform(input: String, context: DiscordContext): ArgumentResult<DiscordEmoji> {
        val emoji = Emojis[input.trim()] ?: return Error(internalLocale.invalidFormat)
        return Success(emoji)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(Emojis.rainbow.unicode)
}
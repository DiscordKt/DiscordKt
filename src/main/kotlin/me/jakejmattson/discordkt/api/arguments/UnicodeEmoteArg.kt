package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.internal.utils.emojiRegex

/**
 * Accepts an emote as a unicode string.
 */
open class UnicodeEmoteArg(override val name: String = "Unicode Emote") : ArgumentType<String>() {
    /**
     * Accepts an emote as a unicode string.
     */
    companion object : UnicodeEmoteArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        return if (emojiRegex.matches(arg))
            Success(arg)
        else
            Error("Invalid format")
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("\uD83D\uDC40")
}
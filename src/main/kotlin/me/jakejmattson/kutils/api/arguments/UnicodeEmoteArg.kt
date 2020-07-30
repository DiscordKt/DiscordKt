package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.internal.utils.emojiRegex

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
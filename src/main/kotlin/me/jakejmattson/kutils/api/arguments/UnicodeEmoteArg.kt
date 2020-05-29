package me.jakejmattson.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.*
import me.aberrantfox.kutils.api.dsl.command.CommandEvent
import me.aberrantfox.kutils.internal.utils.emojiRegex

open class UnicodeEmoteArg(override val name: String = "Unicode Emote") : ArgumentType<String>() {
    companion object : UnicodeEmoteArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        return if (emojiRegex.matches(arg))
            ArgumentResult.Success(arg)
        else
            ArgumentResult.Error("Invalid unicode emote.")
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("\uD83D\uDC40")
}
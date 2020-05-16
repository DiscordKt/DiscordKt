package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.utils.emojiRegex

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
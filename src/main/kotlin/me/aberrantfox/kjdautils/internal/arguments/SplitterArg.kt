package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class SplitterArg(override val name: String = "(Separated|Text)") : ArgumentType<List<String>>() {
    companion object : SplitterArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<String>> {
        val joined = args.joinToString(" ")

        if (!joined.contains(separatorCharacter)) return ArgumentResult.Success(listOf(joined), args.size)

        return ArgumentResult.Success(joined.split(separatorCharacter).toList(), args.size)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("one | two | three")
}
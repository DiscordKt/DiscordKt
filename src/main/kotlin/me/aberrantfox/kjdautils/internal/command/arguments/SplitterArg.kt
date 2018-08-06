package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.separatorCharacter

object SplitterArg : ArgumentType {
    override val examples = arrayListOf("sentence one | Sentence two | Sentence three", "one | two")
    override val name = "(Separated|Text)"
    override val consumptionType = ConsumptionType.All
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val joined = args.joinToString(" ")

        if (!joined.contains(separatorCharacter)) return ArgumentResult.Multiple(listOf(joined), args)

        return ArgumentResult.Multiple(joined.split(separatorCharacter).toList(), args)
    }
}
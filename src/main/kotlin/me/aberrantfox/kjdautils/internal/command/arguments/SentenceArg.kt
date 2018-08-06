package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

object SentenceArg : ArgumentType {
    override val examples = arrayListOf("Hi there", "abc one to three", "This is a sample sentence.")
    override val name = "Text"
    override val consumptionType = ConsumptionType.All
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = ArgumentResult.Multiple(args.joinToString(" "), args)
}
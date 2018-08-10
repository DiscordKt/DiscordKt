package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class SentenceArg(override val name : String = "Text") : ArgumentType {
    companion object : SentenceArg()

    override val examples = arrayListOf("Hi there", "abc one to three", "This is a sample sentence.")
    override val consumptionType = ConsumptionType.All
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = ArgumentResult.Multiple(args.joinToString(" "), args)
}
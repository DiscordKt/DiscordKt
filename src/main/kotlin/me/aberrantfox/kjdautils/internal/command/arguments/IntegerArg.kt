package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.isInteger
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

object IntegerArg : ArgumentType {
    override val examples = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    override val name = "Integer"
    override val consumptionType = ConsumptionType.Single
    override fun isValid(arg: String, event: CommandEvent) = arg.isInteger()
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = ArgumentResult.Single(arg.toInt())
}

package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.convertTimeString

object TimeStringArg : ArgumentType {
    override val consumptionType = ConsumptionType.Multiple
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = convertTimeString(args)
}
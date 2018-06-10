package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.convertTimeString

object TimeStringArg : ArgumentType {
    override val examples = arrayListOf("1h 2m 10 seconds", "5 seconds", "5h", "1d", "1 day", "10 minutes 8 seconds")
    override val name = "Time"
    override val consumptionType = ConsumptionType.Multiple
    override fun isValid(arg: String, event: CommandEvent) = true
    override fun convert(arg: String, args: List<String>, event: CommandEvent) = convertTimeString(args)
}
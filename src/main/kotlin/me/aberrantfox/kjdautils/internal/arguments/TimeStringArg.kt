package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class TimeStringArg(override val name : String = "Time"): ArgumentType<Double>() {
    companion object : TimeStringArg()

    override val consumptionType = ConsumptionType.Multiple
    override val examples = mutableListOf("5 seconds", "5s")

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = convertTimeString(args)
}
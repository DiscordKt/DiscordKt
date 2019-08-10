package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class OnOffArg(override val name: String = "On or Off") : ArgumentType {
    companion object : OnOffArg()

    override val examples = arrayListOf("On", "Off")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
        when (arg.toLowerCase()) {
            "on" -> ArgumentResult.Single(true)
            "off" -> ArgumentResult.Single(false)
            else -> ArgumentResult.Error("Invalid argument. Expected `on` or `off`.")
        }
}
package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class YesNoArg(override val name: String = "Yes or No") : ArgumentType {
    companion object : YesNoArg()

    override val examples = arrayListOf("Yes", "No")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
        when (arg.toLowerCase()) {
            "yes" -> ArgumentResult.Single(true)
            "no" -> ArgumentResult.Single(false)
            else -> ArgumentResult.Error("Invalid argument. Expected `yes` or `no`.")
        }
}
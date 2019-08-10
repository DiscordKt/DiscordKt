package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

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
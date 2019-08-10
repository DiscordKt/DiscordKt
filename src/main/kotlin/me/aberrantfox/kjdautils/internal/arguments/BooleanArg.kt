package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.isBooleanValue
import me.aberrantfox.kjdautils.extensions.stdlib.toBooleanValue
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class BooleanArg(override val name: String = "Boolean") : ArgumentType {
    companion object : BooleanArg()

    override val examples = arrayListOf("True", "true", "T")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
        if (arg.isBooleanValue())
            ArgumentResult.Single(arg.toBooleanValue())
        else
            ArgumentResult.Error("Invalid boolean argument.")
}

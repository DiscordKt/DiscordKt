package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.*

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

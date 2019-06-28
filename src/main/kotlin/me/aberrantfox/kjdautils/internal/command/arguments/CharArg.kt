package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class CharArg(override val name : String = "Character") : ArgumentType {
    companion object : CharArg()

    override val examples = arrayListOf("a", "b", "c")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
        if (arg.length == 1) ArgumentResult.Single(arg[0]) else ArgumentResult.Error("Invalid character argument.")
}

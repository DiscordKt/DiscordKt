package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.isInteger
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class IntegerArg(override val name: String = "Integer") : ArgumentType {
    companion object : IntegerArg()

    override val examples = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
            arg.toIntOrNull()?.let { ArgumentResult.Single(it) } ?: ArgumentResult.Error(
                    "Expected an integer number, got $arg")
}

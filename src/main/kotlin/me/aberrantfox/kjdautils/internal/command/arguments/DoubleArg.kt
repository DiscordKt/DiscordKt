package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.isDouble
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

object DoubleArg : ArgumentType {
    override val examples = arrayListOf("2.3", "5.6", "64.442234", "664.3443", "25.00")
    override val name = "Decimal"
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
            arg.toDoubleOrNull()?.let { ArgumentResult.Single(it) } ?: ArgumentResult.Error(
                    "Expected a decimal number, got $arg")
}
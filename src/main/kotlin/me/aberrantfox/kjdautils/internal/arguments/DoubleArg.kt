package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class DoubleArg(override val name: String = "Decimal"): ArgumentType<Double>() {
    companion object : DoubleArg()

    override val examples = arrayListOf("2.3", "5.6", "64.442234", "664.3443", "25.00")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Double> {
        val double = arg.toDoubleOrNull()
            ?: return ArgumentResult.Error("Expected a decimal number, got $arg")

        return ArgumentResult.Success(double)
    }
}
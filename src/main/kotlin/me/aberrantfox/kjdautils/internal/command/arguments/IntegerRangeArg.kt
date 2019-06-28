package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.randomInt
import me.aberrantfox.kjdautils.internal.command.*

open class IntegerRangeArg(val min: Int = 0, val max: Int, override val name: String = "Integer ($min-$max)") : ArgumentType {
    companion object : IntegerRangeArg(min = 0, max = 10)

    override val examples = arrayListOf(randomInt(min, max).toString())
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {

        val integerArg = arg.toIntOrNull() ?: return ArgumentResult.Error("Argument must be an integer.")

        if (integerArg !in min..max) return ArgumentResult.Error("Argument not in range $min-$max.")

        return ArgumentResult.Single(integerArg)
    }
}

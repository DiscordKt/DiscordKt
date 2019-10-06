package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.stdlib.randomInt
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class IntegerRangeArg(val min: Int = 0, val max: Int, override val name: String = "Integer ($min-$max)"): ArgumentType<Int> {
    companion object : IntegerRangeArg(min = 0, max = 10)

    override val examples = arrayListOf(randomInt(min, max).toString())
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {

        val integerArg = arg.toIntOrNull() ?: return ArgumentResult.Error("Argument must be an integer.")

        if (integerArg !in min..max) return ArgumentResult.Error("Argument not in range $min-$max.")

        return ArgumentResult.Success(integerArg)
    }
}

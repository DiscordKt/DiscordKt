package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class IntegerRangeArg(val min: Int = 0, val max: Int, override val name: String = "Integer ($min-$max)") : ArgumentType<Int>() {
    companion object : IntegerRangeArg(min = 0, max = 10)

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {

        val integerArg = arg.toIntOrNull() ?: return ArgumentResult.Error("Argument must be an integer.")

        if (integerArg !in min..max) return ArgumentResult.Error("Argument not in range $min-$max.")

        return ArgumentResult.Success(integerArg)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf((min..max).random().toString())
}

package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class IntegerArg(override val name: String = "Integer"): ArgumentType<Int>() {
    companion object : IntegerArg()

    override val examples = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {
        val int = arg.toIntOrNull() ?: return ArgumentResult.Error("Expected an integer number, got $arg")
        return ArgumentResult.Success(int)
    }
}

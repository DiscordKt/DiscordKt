package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import java.lang.IllegalArgumentException

open class BooleanArg(override val name: String = "Boolean", val truthValue: String = "true", val falseValue: String = "false"): ArgumentType<Boolean> {
    companion object : BooleanArg()

    override val examples = arrayListOf(truthValue, falseValue)
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Boolean> {

        if (truthValue.isEmpty() || falseValue.isEmpty())
            throw IllegalArgumentException("Custom BooleanArg options cannot be empty!")

        if (truthValue.toLowerCase() == falseValue.toLowerCase())
            throw IllegalArgumentException("Custom BooleanArg options cannot be the same!")

        return when (arg.toLowerCase()) {
            truthValue.toLowerCase() -> ArgumentResult.Success(true)
            falseValue.toLowerCase() -> ArgumentResult.Success(false)
            else -> ArgumentResult.Error("Invalid boolean argument. Expected `$truthValue` or `$falseValue`.")
        }
    }
}

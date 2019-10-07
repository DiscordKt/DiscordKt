package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class BooleanArg(override val name: String = "Boolean", val truthValue: String = "true", val falseValue: String = "false"): ArgumentType<Boolean>() {
    companion object : BooleanArg()

    override val examples = arrayListOf(truthValue, falseValue)
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Boolean> {

        require(!(truthValue.isEmpty() || falseValue.isEmpty())) { "Custom BooleanArg options cannot be empty!" }

        require(truthValue.toLowerCase() != falseValue.toLowerCase()) { "Custom BooleanArg options cannot be the same!" }

        return when (arg.toLowerCase()) {
            truthValue.toLowerCase() -> ArgumentResult.Success(true)
            falseValue.toLowerCase() -> ArgumentResult.Success(false)
            else -> ArgumentResult.Error("Invalid boolean argument. Expected `$truthValue` or `$falseValue`.")
        }
    }
}

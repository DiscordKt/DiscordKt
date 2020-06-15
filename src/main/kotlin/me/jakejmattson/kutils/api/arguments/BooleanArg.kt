package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class BooleanArg(override val name: String = "Boolean", val truthValue: String = "true", val falseValue: String = "false") : ArgumentType<Boolean>() {
    companion object : BooleanArg()

    init {
        require(truthValue.isNotEmpty() && falseValue.isNotEmpty()) { "Custom BooleanArg ($name) options cannot be empty!" }
        require(truthValue.toLowerCase() != falseValue.toLowerCase()) { "Custom BooleanArg ($name) options cannot be the same!" }
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Boolean> {
        return when (arg.toLowerCase()) {
            truthValue.toLowerCase() -> ArgumentResult.Success(true)
            falseValue.toLowerCase() -> ArgumentResult.Success(false)
            else -> ArgumentResult.Error("$name should be `$truthValue` or `$falseValue`.")
        }
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(truthValue, falseValue)
}

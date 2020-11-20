package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent

/**
 * Accepts either of two values. Defaults to true/false.
 *
 * @param truthValue The string value that results in true.
 * @param falseValue The string value that results in false.
 */
open class BooleanArg(override val name: String = "Boolean", private val truthValue: String = "true", private val falseValue: String = "false") : ArgumentType<Boolean>() {
    /**
     * Accepts either true or false.
     */
    companion object : BooleanArg()

    init {
        require(truthValue.isNotEmpty() && falseValue.isNotEmpty()) { "Custom BooleanArg ($name) options cannot be empty!" }
        require(truthValue.toLowerCase() != falseValue.toLowerCase()) { "Custom BooleanArg ($name) options cannot be the same!" }
    }

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Boolean> {
        return when (arg.toLowerCase()) {
            truthValue.toLowerCase() -> Success(true)
            falseValue.toLowerCase() -> Success(false)
            else -> Error("Must be '$truthValue' or '$falseValue'")
        }
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf(truthValue, falseValue)
}

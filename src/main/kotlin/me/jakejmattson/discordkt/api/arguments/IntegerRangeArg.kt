package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*

/**
 * Accepts an integer within a pre-defined range.
 */
open class IntegerRangeArg(private val min: Int, private val max: Int, override val name: String = "Integer ($min-$max)") : ArgumentType<Int>() {

    init {
        require(max > min) { "Maximum value must be greater than minimum value." }
    }

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>): ArgumentResult<Int> {
        val int = arg.toIntOrNull()
            ?: return Error("Invalid format")

        if (int !in min..max)
            return Error("Not in range $min-$max")

        return Success(int)
    }

    override fun generateExamples(event: GlobalCommandEvent<*>) = listOf((min..max).random().toString())
}

package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class IntegerRangeArg(private val min: Int, private val max: Int, override val name: String = "Integer ($min-$max)") : ArgumentType<Int>() {

    init {
        require(max > min) { "Maximum value must be greater than minimum value." }
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {
        val int = arg.toIntOrNull()
            ?: return ArgumentResult.Error("Expected $name, got $arg.")

        if (int !in min..max)
            return ArgumentResult.Error("$arg not in range $name($min-$max).")

        return ArgumentResult.Success(int)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf((min..max).random().toString())
}

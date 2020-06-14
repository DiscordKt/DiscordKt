package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import kotlin.random.Random

open class IntegerArg(override val name: String = "Integer") : ArgumentType<Int>() {
    companion object : IntegerArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {
        val int = arg.toIntOrNull() ?: return ArgumentResult.Error("Couldn't parse $name from $arg.")
        return ArgumentResult.Success(int)
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(Random.nextInt(0, 10).toString())
}

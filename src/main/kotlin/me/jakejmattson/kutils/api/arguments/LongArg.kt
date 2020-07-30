package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * Accept a whole number in the long range.
 */
open class LongArg(override val name: String = "Long") : ArgumentType<Long>() {
    /**
     * Accept a whole number in the long range.
     */
    companion object : LongArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toLongOrNull()) {
            null -> Error<Long>("Invalid format")
            else -> Success(result)
        }

    override fun generateExamples(event: CommandEvent<*>) = (0..10).map { it.toString() }
}
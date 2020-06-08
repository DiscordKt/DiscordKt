package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

open class Manual(override val name: String = "Unknown") : ArgumentType<Any?>() {
    companion object : Manual()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Any?> = ArgumentResult.Success(args, args.size)

    override fun generateExamples(event: CommandEvent<*>) = listOf("Manual")
}
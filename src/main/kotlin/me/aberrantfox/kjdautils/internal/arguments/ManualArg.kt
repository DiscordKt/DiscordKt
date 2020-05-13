package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class Manual(override val name: String = "Unknown") : ArgumentType<Any?>() {
    companion object : Manual()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Any?> = ArgumentResult.Success(args, args.size)

    override fun generateExamples(event: CommandEvent<*>) = listOf("Manual")
}
package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class TimeArg(override val name: String = "Time") : ArgumentType<Double>() {
    companion object : TimeArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = convertTimeString(args)

    override fun generateExamples(event: CommandEvent<*>) = listOf("5 seconds", "5s")
}
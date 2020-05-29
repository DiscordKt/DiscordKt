package me.jakejmattson.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.ArgumentType
import me.aberrantfox.kutils.api.dsl.command.CommandEvent
import me.aberrantfox.kutils.internal.utils.convertTimeString

open class TimeArg(override val name: String = "Time") : ArgumentType<Double>() {
    companion object : TimeArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = convertTimeString(args)

    override fun generateExamples(event: CommandEvent<*>) = listOf("5 seconds", "5s")
}
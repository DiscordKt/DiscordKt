package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class AnyArg(override val name: String = "Any") : ArgumentType<String>() {
    companion object : AnyArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = ArgumentResult.Success(arg)

    override fun generateExamples(event: CommandEvent<*>) = listOf(name)
}
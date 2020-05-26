package me.aberrantfox.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.*
import me.aberrantfox.kutils.api.dsl.command.CommandEvent

open class AnyArg(override val name: String = "Any") : ArgumentType<String>() {
    companion object : AnyArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = ArgumentResult.Success(arg)

    override fun generateExamples(event: CommandEvent<*>) = listOf(name)
}
package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class Manual(override val name: String = "Unknown"): ArgumentType<Any?>() {
    companion object : Manual()

    override val consumptionType = ConsumptionType.All

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Any?> = ArgumentResult.Success(args, args)

    override fun generateExamples(event: CommandEvent<*>) = mutableListOf("Manual")
}
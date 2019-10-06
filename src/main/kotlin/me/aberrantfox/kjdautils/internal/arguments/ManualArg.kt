package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class Manual(override val name: String = "Unknown"): ArgumentType<Any?>() {
    companion object : Manual()

    override val examples = arrayListOf("None-specified")
    override val consumptionType = ConsumptionType.All
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Any?> = ArgumentResult.Success(args, args)
}
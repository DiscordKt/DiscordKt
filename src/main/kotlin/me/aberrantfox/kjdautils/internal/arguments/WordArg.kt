package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

open class WordArg(override val name : String = "Word") : ArgumentType<String>() {
    companion object : WordArg()

    override val consumptionType = ConsumptionType.Single

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = ArgumentResult.Success(arg)

    override fun generateExamples(event: CommandEvent<*>) = mutableListOf("Word")
}
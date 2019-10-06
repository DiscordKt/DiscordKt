package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class WordArg(override val name : String = "Word") : ArgumentType<String>() {
    companion object : WordArg()

    override val examples = arrayListOf("exampleWord", "123", "bob")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = ArgumentResult.Success(arg)
}
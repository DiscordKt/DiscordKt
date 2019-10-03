package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class JsStringArg(override val name : String = "JsStringArg") : ArgumentType {
    companion object : JsStringArg()
    override val examples = arrayListOf("\"A sample\"")
    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val joined = args.joinToString(" ")
        val countOfQuotes = joined.count { it == '\"' }

        return if(countOfQuotes < 2 || joined.replace("\"","").isBlank()) {
            ArgumentResult.Error("JSStringArgs must have be surrounded by quotes.")
        } else {
            val arg = joined.split('\"').component2()
            val consumed = if(arg.contains(" ")) {
                "\"$arg\"".split(" ")
            } else {
                listOf("\"$arg\"")
            }

            ArgumentResult.Multiple(arg, consumed)
        }
    }
}
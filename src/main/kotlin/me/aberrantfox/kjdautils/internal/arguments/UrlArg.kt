package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.stdlib.containsURl
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class UrlArg(override val name : String = "URL"): ArgumentType<String>() {
    companion object : UrlArg()

    override val examples = arrayListOf("http://www.google.com", "https://www.youtube.co.uk")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        return if (arg.containsURl())
            ArgumentResult.Success(arg)
        else
            ArgumentResult.Error("Expected a URL, got $arg")
    }
}
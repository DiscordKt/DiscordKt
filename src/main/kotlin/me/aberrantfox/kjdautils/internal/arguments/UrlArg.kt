package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.containsURl
import me.aberrantfox.kjdautils.internal.command.*

open class UrlArg(override val name: String = "URL") : ArgumentType<String>() {
    companion object : UrlArg()

    override val consumptionType = ConsumptionType.Single

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        return if (arg.containsURl())
            ArgumentResult.Success(arg)
        else
            ArgumentResult.Error("Expected a URL, got $arg")
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("http://www.google.com")
}
package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.internal.command.*

open class CommandArg(override val name: String = "Command"): ArgumentType<Command>() {
    companion object : CommandArg()
    
    override val examples = arrayListOf("Help", "Ping")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Command> {
        val command = event.container[arg.toLowerCase()]

        return if (command != null) {
            ArgumentResult.Success(command)
        } else {
            ArgumentResult.Error("Couldn't find command: $arg")
        }
    }
}
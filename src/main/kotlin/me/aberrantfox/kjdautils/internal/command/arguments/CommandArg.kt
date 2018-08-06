package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

object CommandArg : ArgumentType {
    override val examples = arrayListOf("Help", "Ping")
    override val name = "Command"
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val command = event.container[arg.toLowerCase()]

        return if (command != null) {
            ArgumentResult.Single(command)
        } else {
            ArgumentResult.Error("Couldn't find command: $arg")
        }
    }
}
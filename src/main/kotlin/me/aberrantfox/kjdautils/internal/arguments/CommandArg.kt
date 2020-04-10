package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.internal.command.*

open class CommandArg(override val name: String = "Command"): ArgumentType<Command>() {
    companion object : CommandArg()

    override val consumptionType = ConsumptionType.Single
    override val examples = arrayListOf("Help")
    override var exampleFactory = createExampleFactory {
        it.container.commands.mapNotNull { it.names.firstOrNull() }.toMutableList()
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Command> {
        val command = event.container[arg.toLowerCase()]
            ?: return ArgumentResult.Error("Couldn't find command: $arg")

        return ArgumentResult.Success(command)
    }
}
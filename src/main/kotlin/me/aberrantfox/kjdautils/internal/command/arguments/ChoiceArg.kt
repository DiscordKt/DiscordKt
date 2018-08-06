package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.isBooleanValue
import me.aberrantfox.kjdautils.extensions.stdlib.toBooleanValue
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

/**
 * ChoiceArg takes in any number of arbitrary objects and maps arguments that match it's toString() string
 * Example: the companion object allows two choices "true" or "false" and will automatically convert to Boolean
 */
open class ChoiceArg(vararg choices: Any) : ArgumentType {
    companion object BinaryChoiceArg : ChoiceArg(true, false)

    private val enumerations = choices.associateBy { it.toString().toLowerCase() }

    override val examples = ArrayList(enumerations.keys)
    override val name = "Choice"
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
            enumerations[arg.toLowerCase()]?.let (ArgumentResult::Single) ?: ArgumentResult.Error(
                    "Invalid choice. Available choices: ${enumerations.keys.joinToString (", ")}")
}
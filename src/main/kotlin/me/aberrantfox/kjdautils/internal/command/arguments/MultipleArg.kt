package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

class MultipleArg(val base: ArgumentType, name: String = ""): ArgumentType {
    override val name = if (name.isNotBlank()) name else "${base.name}..."
    override val examples = ArrayList(base.examples.chunked(2).map { it.joinToString(" ") })
    override val consumptionType = ConsumptionType.Multiple
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val result = mutableListOf<Any>()
        val consumed = mutableListOf<String>()

        args.forEach {
            val subResult = base.convert(it, args, event) // if in the future we need multiple multiple.. fix this
            when (subResult) {
                is ArgumentResult.Single -> {
                    result.add(subResult.result)
                    consumed.add(it)
                }
                is ArgumentResult.Multiple -> {
                    result.add(subResult.result)
                    consumed.addAll(subResult.consumed)
                }
                is ArgumentResult.Error -> return@forEach
            }
        }

        return ArgumentResult.Multiple(result.toList(), consumed)
    }
}
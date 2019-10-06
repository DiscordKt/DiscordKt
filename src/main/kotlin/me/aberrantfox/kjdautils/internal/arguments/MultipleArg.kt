package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

class MultipleArg(val base: ArgumentType<*>, name: String = ""): ArgumentType<List<*>>() {
    override val name = if (name.isNotBlank()) name else "${base.name}..."
    override val examples = ArrayList(base.examples.chunked(2).map { it.joinToString(" ") })
    override val consumptionType = ConsumptionType.Multiple
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<*>> {
        val result = mutableListOf<Any?>()
        val consumed = mutableListOf<String>()

        args.forEach {
            val subResult = base.convert(it, args, event) // if in the future we need multiple multiple.. fix this
            when (subResult) {
                is ArgumentResult.Success -> {
                    result.add(subResult.result)

                    if (subResult.consumed.isNotEmpty())
                        consumed.addAll(subResult.consumed)
                    else
                        consumed.add(it)
                }
                is ArgumentResult.Error -> return@forEach
            }
        }

        return ArgumentResult.Success(result.toList(), consumed)
    }
}
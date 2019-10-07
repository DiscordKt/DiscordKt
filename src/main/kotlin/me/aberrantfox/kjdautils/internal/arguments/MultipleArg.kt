package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

class MultipleArg<T>(val base: ArgumentType<T>, name: String = ""): ArgumentType<List<T>>() {
    override val name = if (name.isNotBlank()) name else "${base.name}..."
    override val examples = ArrayList(base.examples.chunked(2).map { it.joinToString(" ") })
    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<T>> {
        val result = mutableListOf<T>()
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

        return ArgumentResult.Success(result, consumed)
    }
}
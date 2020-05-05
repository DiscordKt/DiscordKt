package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

class MultipleArg<T>(val base: ArgumentType<T>, name: String = "") : ArgumentType<List<T>>() {
    override val name = if (name.isNotBlank()) name else "${base.name}..."
    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<T>> {
        val totalResult = mutableListOf<T>()
        val totalConsumed = mutableListOf<String>()

        args.forEach {
            with(base.convert(it, args, event)) {
                when (this) {
                    is ArgumentResult.Success -> {
                        totalResult.add(result)

                        if (consumed.isNotEmpty())
                            totalConsumed.addAll(consumed)
                        else
                            totalConsumed.add(it)
                    }
                    is ArgumentResult.Error -> {
                        if (totalResult.isEmpty())
                            return ArgumentResult.Error(this.error)
                        else
                            return@forEach
                    }
                }
            }
        }

        return ArgumentResult.Success(totalResult, totalConsumed)
    }

    override fun generateExamples(event: CommandEvent<*>) =
        base.generateExamples(event).chunked(2).map { it.joinToString(" ") }
}
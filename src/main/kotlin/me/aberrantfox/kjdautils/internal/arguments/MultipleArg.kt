package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

class MultipleArg<T>(val base: ArgumentType<T>, name: String = "") : ArgumentType<List<T>>() {
    override val name = if (name.isNotBlank()) name else "${base.name}..."
    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<T>> {
        val totalResult = mutableListOf<T>()
        val totalConsumed = mutableListOf<String>()
        val remainingArgs = args.toMutableList()

        complete@ while (remainingArgs.isNotEmpty()) {
            val currentArg = remainingArgs.first()
            val conversion = base.convert(currentArg, remainingArgs, event)

            when (conversion) {
                is ArgumentResult.Success -> {
                    totalResult.add(conversion.result)

                    val consumed = conversion.consumed.takeIf { it.isNotEmpty() } ?: listOf(currentArg)
                    totalConsumed.addAll(consumed)
                    consumed.forEach { remainingArgs.remove(it) }
                }
                is ArgumentResult.Error -> {
                    if (totalResult.isEmpty())
                        return ArgumentResult.Error(conversion.error)

                    break@complete
                }
            }
        }

        return ArgumentResult.Success(totalResult, totalConsumed)
    }

    override fun generateExamples(event: CommandEvent<*>) =
        base.generateExamples(event).chunked(2).map { it.joinToString(" ") }
}
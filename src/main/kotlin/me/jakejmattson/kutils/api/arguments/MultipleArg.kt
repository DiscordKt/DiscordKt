package me.jakejmattson.kutils.api.arguments

import me.aberrantfox.kutils.api.dsl.arguments.*
import me.aberrantfox.kutils.api.dsl.command.CommandEvent

class MultipleArg<T>(val base: ArgumentType<T>, name: String = "") : ArgumentType<List<T>>() {
    override val name = if (name.isNotBlank()) name else "${base.name}..."

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<List<T>> {
        val totalResult = mutableListOf<T>()
        var totalConsumed = 0
        val remainingArgs = args.toMutableList()

        complete@ while (remainingArgs.isNotEmpty()) {
            val currentArg = remainingArgs.first()
            val conversion = base.convert(currentArg, remainingArgs, event)

            when (conversion) {
                is ArgumentResult.Success -> {
                    totalResult.add(conversion.result)
                    val consumed = conversion.consumed
                    totalConsumed += consumed

                    remainingArgs.subList(0, consumed).toList().forEach { remainingArgs.remove(it) }
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
package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * Accepts multiple arguments of the given type. Returns a list.
 *
 * @param base The [ArgumentType] that you expect to be used to create the list.
 */
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
                is Success -> {
                    totalResult.add(conversion.result)
                    val consumed = conversion.consumed
                    totalConsumed += consumed

                    remainingArgs.subList(0, consumed).toList().forEach { remainingArgs.remove(it) }
                }
                is Error -> {
                    if (totalResult.isEmpty())
                        return Error(conversion.error)

                    break@complete
                }
            }
        }

        return Success(totalResult, totalConsumed)
    }

    override fun generateExamples(event: CommandEvent<*>) =
        base.generateExamples(event).chunked(2).map { it.joinToString(" ") }
}
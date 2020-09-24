package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.*

/**
 * Accepts multiple arguments of the given type. Returns a list.
 *
 * @param base The [ArgumentType] that you expect to be used to create the list.
 */
class MultipleArg<T>(val base: ArgumentType<T>, name: String = "") : ArgumentType<List<T>>() {
    override val name = if (name.isNotBlank()) name else "${base.name}..."

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>): ArgumentResult<List<T>> {
        val totalResult = mutableListOf<T>()
        var totalConsumed = 0
        val remainingArgs = args.toMutableList()

        complete@ while (remainingArgs.isNotEmpty()) {
            when (val conversion = base.convert(remainingArgs.first(), remainingArgs, event)) {
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

    override fun generateExamples(event: GlobalCommandEvent<*>) =
        base.generateExamples(event).chunked(2).map { it.joinToString(" ") }

    override fun formatData(data: List<T>) = "[${data.joinToString { base.formatData(it) }}]"
}
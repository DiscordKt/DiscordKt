package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * Accepts an integer within a pre-defined range.
 */
public open class IntegerRangeArg(private val min: Int,
                                  private val max: Int,
                                  override val name: String = "Integer ($min-$max)",
                                  override val description: String = internalLocale.integerRangeArgDescription.inject(min.toString(), max.toString())) : Argument<Int> {
    init {
        require(max > min) { "Maximum value must be greater than minimum value." }
    }

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Int> {
        val int = arg.toIntOrNull()
            ?: return Error(internalLocale.invalidFormat)

        if (int !in min..max)
            return Error("Not in range $min-$max")

        return Success(int)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf((min..max).random().toString())
}
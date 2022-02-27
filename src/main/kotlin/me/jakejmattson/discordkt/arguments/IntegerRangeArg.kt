package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * Accepts an integer within a pre-defined range.
 */
public open class IntegerRangeArg(private val min: Int,
                                  private val max: Int,
                                  override val name: String = "Integer ($min-$max)",
                                  override val description: String = internalLocale.integerRangeArgDescription.inject(min.toString(), max.toString())) : IntegerArgument<Int> {
    init {
        require(max > min) { "Maximum value must be greater than minimum value." }
    }

    override suspend fun transform(input: Int, context: DiscordContext): Result<Int> {
        if (input !in min..max)
            return Error("Not in range $min-$max")

        return Success(input)
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf((min..max).random().toString())
}
package me.jakejmattson.discordkt.arguments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.inject

/**
 * Accepts an integer within a pre-defined range.
 */
public open class IntegerRangeArg(
    private val min: Int,
    private val max: Int,
    override val name: String = "Integer ($min-$max)",
    override val description: String = internalLocale.integerRangeArgDescription.inject(min.toString(), max.toString())
) : IntegerArgument<Int> {
    init {
        require(max > min) { "Maximum value must be greater than minimum value." }
    }

    override suspend fun transform(input: Int, context: DiscordContext): Either<String, Int> = either {
        ensure(input in min..max) {
            "Not in range $min-$max"
        }

        input
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> =
        listOf((min..max).random().toString())
}
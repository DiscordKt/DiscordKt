package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.extensions.consumeFirst
import kotlin.random.Random

/**
 * An [Argument] that accepts a primitive type.
 */
public interface PrimitiveArgument<Input, Output> : Argument<Input, Output> {
    /**
     * Offer autocomplete options for this argument.
     */
    public fun autocomplete(choices: suspend AutocompleteData.() -> List<Input>): AutocompleteArg<Input, Output> {
        return AutocompleteArg(name, description, this, choices)
    }
}

/**
 * An [Argument] that accepts a [String].
 */
public interface StringArgument<Output> : PrimitiveArgument<String, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): String? = args.consumeFirst().takeIf { it.isNotEmpty() }
    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(name)
}

/**
 * An [Argument] that accepts an [Int].
 */
public interface IntegerArgument<Output> : PrimitiveArgument<Int, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Int? = args.consumeFirst().toIntOrNull()
    override suspend fun generateExamples(context: DiscordContext): List<String> = (0..10).map { it.toString() }
}

/**
 * An [Argument] that accepts a [Double].
 */
public interface DoubleArgument<Output> : PrimitiveArgument<Double, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Double? = args.consumeFirst().toDoubleOrNull()
    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("%.2f".format(Random.nextDouble(0.00, 9.99)))
}

/**
 * An [Argument] that accepts a [Boolean].
 *
 * @property truthValue The string value that results in true.
 * @property falseValue The string value that results in false.
 */
public interface BooleanArgument<Output> : PrimitiveArgument<Boolean, Output> {
    public val truthValue: String
    public val falseValue: String

    override suspend fun parse(args: MutableList<String>, discord: Discord): Boolean? {
        return when (args.consumeFirst().lowercase()) {
            truthValue.lowercase() -> true
            falseValue.lowercase() -> false
            else -> null
        }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(truthValue, falseValue)
}
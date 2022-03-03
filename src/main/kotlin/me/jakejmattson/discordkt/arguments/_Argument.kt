@file:Suppress("unused")

package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.interaction.GuildAutoCompleteInteraction
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.extensions.consumeFirst
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import kotlin.random.Random

/**
 * An object that represents a type and contains the logic to convert string arguments to the desired type.
 *
 * @property name The display name for this type in documentations and examples.
 * @property description A description of the data that this type represents.
 */
public sealed interface Argument<Input, Output> : Cloneable {
    public val name: String
    public val description: String

    /**
     * Accept multiple inputs of this Argument.
     */
    public fun multiple(): MultipleArg<Input, Output> = MultipleArg(this)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type.
     */
    public fun optional(default: Output): OptionalArg<Input, Output, Output> = OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type.
     */
    public fun optional(default: suspend (DiscordContext) -> Output): OptionalArg<Input, Output, Output> = OptionalArg(name, this, default)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type - can also be null.
     */
    public fun optionalNullable(default: Output? = null): OptionalArg<Input, Output, Output?> = OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type - can also be null.
     */
    public fun optionalNullable(default: suspend (DiscordContext) -> Output?): OptionalArg<Input, Output, Output?> = OptionalArg(name, this, default)

    /**
     * Parse string input into the correct type handled by this argument.
     *
     * @param args A list of string arguments.
     * @param discord The [Discord] object used to resolve discord entities.
     */
    public suspend fun parse(args: MutableList<String>, discord: Discord): Input?

    /**
     * Transforms a value produced by a slash command or by the [parse] function.
     *
     * @param input The input data of the type [Input]
     * @param context The [DiscordContext] created by the execution of the command.
     * @return [Result] subtype [Success] or [Error].
     */
    public suspend fun transform(input: Input, context: DiscordContext): Result<Output> = Success(input as Output)

    /**
     * A function called whenever an example of this type is needed.
     *
     * @param context Allows the list result to be generated with the relevant discord context.
     */
    public suspend fun generateExamples(context: DiscordContext): List<String>

    /**
     * Utility function to check that this Argument is an [OptionalArg]
     */
    public fun isOptional(): Boolean =  this is OptionalArg<*, *, *>
}

public data class AutocompleteData(public val interaction: GuildAutoCompleteInteraction,
                                   public val input: String)

/**
 * An [Argument] that accepts a primitive type.
 */
public interface PrimitiveArgument<Input, Output> : Argument<Input, Output> {
    public fun autocomplete(choices: suspend AutocompleteData.() -> List<Input>): AutocompleteArg<Input, Output> {
        return AutocompleteArg(name, description, this, choices)
    }
}

/**
 * An [Argument] that accepts a discord entity.
 */
public interface EntityArgument<Input, Output> : Argument<Input, Output>

/**
 * An [Argument] that wraps around another argument.
 */
public interface WrappedArgument<Input, Output, Input2, Output2> : Argument<Input2, Output2> {
    /**
     * The [Argument] that is wrapped.
     */
    public val type: Argument<Input, Output>

    public val innerType: Argument<Input, Output>
        get() {
            var inner: Argument<*, *> = type

            while (inner is WrappedArgument<*, *, *, *>)
                inner = inner.type

            return inner as Argument<Input, Output>
        }

    override suspend fun parse(args: MutableList<String>, discord: Discord): Input2? = type.parse(args, discord) as Input2?
    override suspend fun generateExamples(context: DiscordContext): List<String> = type.generateExamples(context)
}

public inline fun <reified T> WrappedArgument<*, *, *, *>.containsType(): Boolean {
    var innerType: Argument<*, *> = this

    while (innerType is WrappedArgument<*, *, *, *>) {
        if (innerType is T)
            return true

        innerType = innerType.type
    }

    return innerType is T
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

/**
 * An [Argument] that accepts a [User].
 */
public interface UserArgument<Output> : EntityArgument<User, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): User? {
        return args.consumeFirst().toSnowflakeOrNull()?.let { discord.kord.getUser(it) }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.author.mention)
}

/**
 * An [Argument] that accepts a [Role].
 */
public interface RoleArgument<Output> : EntityArgument<Role, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Role? {
        val roles = discord.kord.guilds.toList().flatMap { it.roles.toList() }
        val snowflake = args.consumeFirst().toSnowflakeOrNull()
        return roles.find { it.id == snowflake }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("@everyone")
}

/**
 * An [Argument] that accepts a [Channel].
 */
public interface ChannelArgument<Output> : EntityArgument<Channel, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Channel? {
        return args.consumeFirst().toSnowflakeOrNull()?.let { discord.kord.getChannel(it) }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.channel.mention)
}

/**
 * An [Argument] that accepts an [Attachment].
 */
public interface AttachmentArgument<Output> : EntityArgument<Attachment, Output>

/**
 * The result of some conversion.
 */
public sealed class Result<T>

/**
 * Result indicating that a conversion was successful.
 *
 * @param result The conversion result of the appropriate type.
 */
public data class Success<T>(val result: T) : Result<T>()

/**
 * Result indicating that a conversion was failed.
 *
 * @param error The reason why the conversion failed.
 */
public data class Error<T>(val error: String) : Result<T>()
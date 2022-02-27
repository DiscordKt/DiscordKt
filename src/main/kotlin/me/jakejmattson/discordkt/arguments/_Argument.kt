@file:Suppress("unused")

package me.jakejmattson.discordkt.arguments

import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
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

    public suspend fun parse(args: MutableList<String>, discord: Discord): Input?

    /**
     * Consumes an argument or multiple arguments and converts them into some desired type.
     *
     * @param input The
     * @param context The CommandEvent<*> triggered by the execution of the command.
     * @return ArgumentResult subtype [Success] or [Error].
     */
    public suspend fun transform(input: Input, context: DiscordContext): Result<Output> = Success(input as Output)

    /**
     * A function called whenever an example of this type is needed.
     *
     * @param context Allows the list result to be generated with the relevant discord context.
     */
    public suspend fun generateExamples(context: DiscordContext): List<String>
}

public interface SimpleArgument<Input, Output> : Argument<Input, Output>
public interface EntityArgument<Input, Output> : Argument<Input, Output>

public interface WrappedArgument<Input, Output, Input2, Output2> : Argument<Input2, Output2> {
    public val base: Argument<Input, Output>
}

public interface StringArgument<Output> : SimpleArgument<String, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): String? = args.consumeFirst().takeIf { it.isNotEmpty() }
    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(name)
}

public interface IntegerArgument<Output> : SimpleArgument<Int, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Int? = args.consumeFirst().toIntOrNull()
    override suspend fun generateExamples(context: DiscordContext): List<String> = (0..10).map { it.toString() }
}

public interface DoubleArgument<Output> : SimpleArgument<Double, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Double? = args.consumeFirst().toDoubleOrNull()
    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf("%.2f".format(Random.nextDouble(0.00, 9.99)))
}

public interface BooleanArgument<Output> : SimpleArgument<Boolean, Output> {
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

public interface UserArgument<Output> : EntityArgument<User, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): User? {
        return args.consumeFirst().toSnowflakeOrNull()?.let { discord.kord.getUser(it) }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.author.mention)
}

public interface RoleArgument<Output> : EntityArgument<Role, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Role? {
        val roles = discord.kord.guilds.toList().flatMap { it.roles.toList() }
        val snowflake = args.consumeFirst().toSnowflakeOrNull()
        return roles.find { it.id == snowflake }
    }
}

public interface ChannelArgument<Output> : EntityArgument<Channel, Output> {
    override suspend fun parse(args: MutableList<String>, discord: Discord): Channel? {
        return args.consumeFirst().toSnowflakeOrNull()?.let { discord.kord.getChannel(it) }
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.channel.mention)
}

public interface AttachmentArgument<Output> : EntityArgument<Attachment, Output>

/**
 * The result of an argument conversion.
 */
public sealed class Result<T>

/**
 * ArgumentResult indicating that a conversion was successful.
 *
 * @param result The conversion result of the appropriate type.
 */
public data class Success<T>(val result: T) : Result<T>()

/**
 * ArgumentResult indicating that a conversion was failed.
 *
 * @param error The reason why the conversion failed.
 */
public data class Error<T>(val error: String) : Result<T>()
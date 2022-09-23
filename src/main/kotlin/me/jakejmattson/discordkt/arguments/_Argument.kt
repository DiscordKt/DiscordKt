@file:Suppress("unused")

package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.commands.DiscordContext

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
     * Utility function to check that this Argument is an [OptionalArg].
     */
    public fun isOptional(): Boolean = if (this is WrappedArgument<*, *, *, *>) this.containsType<OptionalArg<*, *, *>>() else false
}

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
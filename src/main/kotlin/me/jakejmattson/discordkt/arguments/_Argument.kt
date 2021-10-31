@file:Suppress("unused")

package me.jakejmattson.discordkt.arguments

import me.jakejmattson.discordkt.commands.CommandEvent

/**
 * An object that represents a type and contains the logic to convert string arguments to the desired type.
 *
 * @property name The display name for this type in documentations and examples.
 * @property description A description of the data that this type represents.
 */
public interface Argument<T> : Cloneable {
    public val name: String
    public val description: String

    /**
     * Accept multiple inputs of this Argument.
     */
    public fun multiple(): MultipleArg<T> = MultipleArg(this)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type.
     */
    public fun optional(default: T): OptionalArg<T> = OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type.
     */
    public fun optional(default: suspend (CommandEvent<*>) -> T): OptionalArg<T> = OptionalArg(name, this, default)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type - can also be null.
     */
    public fun optionalNullable(default: T? = null): OptionalArg<T?> = OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type - can also be null.
     */
    public fun optionalNullable(default: suspend (CommandEvent<*>) -> T?): OptionalArg<T?> = OptionalArg(name, this, default)

    /**
     * Consumes an argument or multiple arguments and converts them into some desired type.
     *
     * @param arg The next argument passed into the command.
     * @param args All remaining arguments passed into the command.
     * @param event The CommandEvent<*> triggered by the execution of the command.
     * @return ArgumentResult subtype [Success] or [Error].
     */
    public suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T>

    /**
     * A function called whenever an example of this type is needed.
     *
     * @param event Allows the list result to be generated with the relevant discord context.
     */
    public suspend fun generateExamples(event: CommandEvent<*>): List<String>

    /**
     * Create a custom formatter for the data this [Argument] produces.
     */
    public fun formatData(data: T): String = data.toString()
}

/**
 * The result of an argument conversion.
 */
public sealed class ArgumentResult<T>

/**
 * ArgumentResult indicating that a conversion was successful.
 *
 * @param result The conversion result of the appropriate type.
 * @param consumed The number of arguments consumed in this operation.
 */
public data class Success<T>(val result: T, val consumed: Int = 1) : ArgumentResult<T>()

/**
 * ArgumentResult indicating that a conversion was failed.
 *
 * @param error The reason why the conversion failed.
 */
public data class Error<T>(val error: String) : ArgumentResult<T>()
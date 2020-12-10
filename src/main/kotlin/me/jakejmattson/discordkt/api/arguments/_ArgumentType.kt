@file:Suppress("unused")

package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.internal.utils.simplerName

/**
 * An object that represents a type and contains the logic to convert string arguments to the desired type.
 *
 * @property name The display name for this type in documentations and examples.
 */
abstract class ArgumentType<T> : Cloneable {
    abstract val name: String

    internal lateinit var defaultValue: suspend (CommandEvent<*>) -> T
        private set

    /**
     * Accept multiple inputs of this ArgumentType.
     */
    fun multiple() = MultipleArg(this, name)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type.
     */
    fun optional(default: T) = OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type.
     */
    fun optional(default: suspend (CommandEvent<*>) -> T) = OptionalArg(name, this, default)

    /**
     * Make this argument optional and fall back to the default value if the conversion fails.
     *
     * @param default A default value matching the expected type - can also be null.
     */
    fun optionalNullable(default: T? = null) = OptionalArg(name, this) { default }

    /**
     * Make this argument optional and fall back to the default value if the conversion fails. Exposes a [CommandEvent].
     *
     * @param default A default value matching the expected type - can also be null.
     */
    fun optionalNullable(default: suspend (CommandEvent<*>) -> T?) = OptionalArg(name, this, default)

    /**
     * Consumes an argument or multiple arguments and converts them into some desired type.
     *
     * @param arg The next argument passed into the command.
     * @param args All remaining arguments passed into the command.
     * @param event The CommandEvent<*> triggered by the execution of the command.
     * @return ArgumentResult subtype [Success] or [Error].
     */
    abstract suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T>

    /**
     * A function called whenever an example of this type is needed.
     *
     * @param event Allows the list result to be generated with the relevant discord context.
     */
    abstract suspend fun generateExamples(event: CommandEvent<*>): List<String>

    /** Determine the simpler name (just the class) and then remove the companion tag */
    override fun toString() = this::class.simplerName

    /**
     * Create a custom formatter for the data this ArgumentType produces.
     */
    open fun formatData(data: T) = data.toString()
}

/**
 * The result of an argument conversion.
 */
sealed class ArgumentResult<T>

/**
 * ArgumentResult indicating that a conversion was successful.
 *
 * @param result The conversion result of the appropriate type.
 * @param consumed The number of arguments consumed in this operation.
 */
data class Success<T>(val result: T, val consumed: Int = 1) : ArgumentResult<T>()

/**
 * ArgumentResult indicating that a conversion was failed.
 *
 * @param error The reason why the conversion failed.
 */
data class Error<T>(val error: String) : ArgumentResult<T>()
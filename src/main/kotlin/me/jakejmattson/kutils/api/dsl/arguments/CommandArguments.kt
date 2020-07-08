@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.arguments

import me.jakejmattson.kutils.api.dsl.command.CommandEvent

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

/**
 * An object that represents a type and contains the logic to convert string arguments to the desired type.
 *
 * @property name The display name for this type in documentations and examples.
 * @sample me.jakejmattson.kutils.api.arguments.IntegerArg
 */
abstract class ArgumentType<T> : Cloneable {
    abstract val name: String

    internal var isOptional: Boolean = false
        private set

    internal var defaultValue: ((CommandEvent<*>) -> T)? = null
        private set

    private fun <T> cloneToOptional() = (clone() as ArgumentType<T>).apply { isOptional = true }
    fun makeOptional(default: T) = cloneToOptional<T>().apply { defaultValue = { default } }
    fun makeOptional(default: (CommandEvent<*>) -> T) = cloneToOptional<T>().apply { defaultValue = default }
    fun makeNullableOptional(default: T? = null) = cloneToOptional<T?>().apply { defaultValue = { default } }
    fun makeNullableOptional(default: (CommandEvent<*>) -> T?) = cloneToOptional<T?>().apply { defaultValue = default }

    /**
     * Consumes an argument or multiple arguments and converts them into some desired type.
     *
     * @param arg The next argument passed into the command.
     * @param args All remaining arguments passed into the command.
     * @param event The CommandEvent triggered by the execution of the command.
     * @return ArgumentResult subtype 'Success' or 'Error'.
     * @see Success
     * @see Error
     */
    abstract fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T>

    /**
     * A function called whenever an example of this type is needed.
     *
     * @param event Allows the list result to be generated with the relevant discord context.
     */
    abstract fun generateExamples(event: CommandEvent<*>): List<String>

    override fun toString() = this::class.toString().substringAfterLast(".").substringBefore("$")
}
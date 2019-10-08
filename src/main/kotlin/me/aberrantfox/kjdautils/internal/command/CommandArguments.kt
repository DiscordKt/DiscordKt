package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import net.dv8tion.jda.api.JDA

sealed class ArgumentResult<T> {
    data class Success<T>(val result: T, val consumed: List<String> = listOf()): ArgumentResult<T>()

    data class Error<T>(val error: String): ArgumentResult<T>()

    inline fun<R> map(mapper: (T) -> R): ArgumentResult<R> {
        return when (this) {
            is Error<T> -> this as ArgumentResult<R>
            is Success<T> -> Success(mapper(result))
        }
    }
}

enum class ConsumptionType {
    Single, Multiple, All
}

abstract class ArgumentType<T>: Cloneable {
    abstract val consumptionType: ConsumptionType
    abstract val examples: ArrayList<String>
    abstract val name: String

    var isOptional: Boolean = false
        private set

    var defaultValue: ((CommandEvent<*>) -> T)? = null
        private set

    fun makeOptional(default: T): ArgumentType<T> {
        val newArg = this.clone() as (ArgumentType<T>)
        newArg.isOptional = true
        newArg.defaultValue = { default }
        return newArg
    }

    fun makeOptional(default: (CommandEvent<*>) -> T): ArgumentType<T> {
        val newArg = this.clone() as (ArgumentType<T>)
        newArg.isOptional = true
        newArg.defaultValue = default
        return newArg
    }

    fun makeNullableOptional(default: T? = null): ArgumentType<T?> {
        val newArg = this.clone() as (ArgumentType<T?>)
        newArg.isOptional = true
        newArg.defaultValue = { default }
        return newArg
    }

    fun makeNullableOptional(default: (CommandEvent<*>) -> T?): ArgumentType<T?> {
        val newArg = this.clone() as (ArgumentType<T?>)
        newArg.isOptional = true
        newArg.defaultValue = default
        return newArg
    }

    abstract fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T>
}

fun tryRetrieveSnowflake(jda: JDA, action: (JDA) -> Any?): Any? =
        try {
            action(jda)
        } catch (e: RuntimeException) {
            null
        }
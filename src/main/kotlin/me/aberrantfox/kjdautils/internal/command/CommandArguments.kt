package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.*
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

interface ArgumentType<T> {
    val consumptionType: ConsumptionType
    val examples: ArrayList<String>
    val name: String

    fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T>
}

fun tryRetrieveSnowflake(jda: JDA, action: (JDA) -> Any?): Any? =
        try {
            action(jda)
        } catch (e: RuntimeException) {
            null
        }
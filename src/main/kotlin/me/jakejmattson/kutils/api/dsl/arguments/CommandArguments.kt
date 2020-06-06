package me.jakejmattson.kutils.api.dsl.arguments

import me.jakejmattson.kutils.api.dsl.command.CommandEvent

sealed class ArgumentResult<T> {
    data class Success<T>(val result: T, val consumed: Int = 1) : ArgumentResult<T>()
    data class Error<T>(val error: String) : ArgumentResult<T>()
}

abstract class ArgumentType<T> : Cloneable {
    abstract val name: String

    var isOptional: Boolean = false
        private set

    var defaultValue: ((CommandEvent<*>) -> T)? = null
        private set

    private fun <T> cloneToOptional() = (clone() as ArgumentType<T>).apply { isOptional = true }
    fun makeOptional(default: T) = cloneToOptional<T>().apply { defaultValue = { default } }
    fun makeOptional(default: (CommandEvent<*>) -> T) = cloneToOptional<T>().apply { defaultValue = default }
    fun makeNullableOptional(default: T? = null) = cloneToOptional<T?>().apply { defaultValue = { default } }
    fun makeNullableOptional(default: (CommandEvent<*>) -> T?) = cloneToOptional<T?>().apply { defaultValue = default }

    abstract fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T>
    abstract fun generateExamples(event: CommandEvent<*>): List<String>

    override fun toString() = this::class.toString().substringAfterLast(".").substringBefore("$")
}
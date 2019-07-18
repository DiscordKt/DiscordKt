package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import net.dv8tion.jda.api.JDA

sealed class ArgumentResult {
    /** A result that has only consumed the single argument passed. **/
    data class Single(val result: Any) : ArgumentResult() {
        companion object
    }

    /** A result that has consumed more than just the argument given. **/
    data class Multiple(val result: Any, val consumed: List<String>) : ArgumentResult() {
        companion object
    }

    data class Error(val error: String) : ArgumentResult() {
        companion object
    }
}

enum class ConsumptionType {
    Single, Multiple, All
}

interface ArgumentType {
    val consumptionType: ConsumptionType
    val examples: ArrayList<String>
    val name: String

    fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult
}

fun tryRetrieveSnowflake(jda: JDA, action: (JDA) -> Any?): Any? =
        try {
            action(jda)
        } catch (e: RuntimeException) {
            null
        }
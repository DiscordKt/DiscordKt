@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.preconditions

import me.jakejmattson.kutils.api.dsl.command.CommandEvent

const val defaultPreconditionPriority = 5

fun precondition(condition: (CommandEvent<*>) -> PreconditionResult): (CommandEvent<*>) -> PreconditionResult = condition

data class PreconditionData(val condition: (CommandEvent<*>) -> PreconditionResult, val priority: Int = defaultPreconditionPriority)

sealed class PreconditionResult
object Pass : PreconditionResult()
data class Fail(val reason: String? = null) : PreconditionResult()
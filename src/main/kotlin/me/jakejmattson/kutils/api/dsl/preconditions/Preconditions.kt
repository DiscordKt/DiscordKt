@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.preconditions

import me.jakejmattson.kutils.api.dsl.command.CommandEvent

const val defaultPreconditionPriority = 5

/**
 * A block representing a precondition that will either [Pass] or [Fail].
 */
fun precondition(condition: (CommandEvent<*>) -> PreconditionResult) = condition

internal data class PreconditionData(val condition: (CommandEvent<*>) -> PreconditionResult, val priority: Int = defaultPreconditionPriority)

sealed class PreconditionResult

/**
 * Object indicating that this precondition has passed.
 */
object Pass : PreconditionResult()

/**
 * Object indicating that this precondition has failed.
 *
 * @param reason The reason for failure.
 */
data class Fail(val reason: String = "") : PreconditionResult()
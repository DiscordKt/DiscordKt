@file:Suppress("unused")

package me.jakejmattson.kutils.api.dsl.preconditions

import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * A class that represents some condition that must Pass before a command can be executed.
 */
abstract class Precondition {
    /**
     * A function that will either [Pass] or [Fail].
     */
    abstract fun evaluate(event: CommandEvent<*>): PreconditionResult
}

/** @suppress Redundant doc */
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
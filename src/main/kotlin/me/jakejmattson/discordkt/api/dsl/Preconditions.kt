@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import me.jakejmattson.discordkt.internal.annotations.BuilderDSL

/** @suppress Redundant doc */
interface PreconditionResult

/**
 * Object indicating that this precondition has passed.
 */
object Pass : PreconditionResult

/**
 * Create a new precondition.
 *
 * @param priority The relative priority of this precondition being run.
 * @param construct The builder function.
 */
@BuilderDSL
fun precondition(priority: Int = 5, construct: CommandEvent<*>.() -> PreconditionResult) = Precondition(priority, construct)

/**
 * This is not for you...
 */
data class Precondition(val priority: Int, private val construct: CommandEvent<*>.() -> PreconditionResult) {
    internal fun evaluate(event: CommandEvent<*>) = construct.invoke(event)
}
package me.jakejmattson.kutils.api.annotations

import me.jakejmattson.kutils.api.dsl.preconditions.defaultPreconditionPriority

@Target(AnnotationTarget.FUNCTION)
/**
 * A condition that must be passed in order to execute a command.
 *
 * @param priority A value representing the order in which this precondition should be called relative to other preconditions.
 */
annotation class Precondition(val priority: Int = defaultPreconditionPriority)
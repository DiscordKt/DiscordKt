package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.internal.command.PreconditionResult

const val defaultPreconditionPriority = 5

annotation class Precondition(val priority: Int = defaultPreconditionPriority)

fun precondition(condition: (CommandEvent<*>) -> PreconditionResult): (CommandEvent<*>) -> PreconditionResult = condition

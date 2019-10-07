package me.aberrantfox.kjdautils.internal.command

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.defaultPreconditionPriority

data class PreconditionData(val condition: (CommandEvent<*>) -> PreconditionResult, val priority: Int = defaultPreconditionPriority)

sealed class PreconditionResult

object Pass : PreconditionResult()
data class Fail(val reason: String? = null) : PreconditionResult()
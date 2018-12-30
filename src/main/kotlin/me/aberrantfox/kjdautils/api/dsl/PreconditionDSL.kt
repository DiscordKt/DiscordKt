package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.internal.command.PreconditionResult

annotation class Precondition

fun precondition(condition: (CommandEvent) -> PreconditionResult): (CommandEvent) -> PreconditionResult = condition

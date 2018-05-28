package me.aberrantfox.kjdautils.internal.command

sealed class PreconditionResult

object Pass : PreconditionResult()
data class Fail(val reason: String? = null) : PreconditionResult()
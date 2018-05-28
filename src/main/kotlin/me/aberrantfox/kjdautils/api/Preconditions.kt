package me.aberrantfox.kjdautils.api

sealed class PreconditionResult

object Pass : PreconditionResult()
data class Fail(val reason: String? = null) : PreconditionResult()
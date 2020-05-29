package me.jakejmattson.kutils.api.annotations

import me.aberrantfox.kutils.api.dsl.preconditions.defaultPreconditionPriority

@Target(AnnotationTarget.FUNCTION)
annotation class Precondition(val priority: Int = defaultPreconditionPriority)
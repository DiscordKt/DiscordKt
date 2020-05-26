package me.aberrantfox.kutils.api.annotations

import me.aberrantfox.kutils.api.dsl.preconditions.defaultPreconditionPriority

annotation class Precondition(val priority: Int = defaultPreconditionPriority)
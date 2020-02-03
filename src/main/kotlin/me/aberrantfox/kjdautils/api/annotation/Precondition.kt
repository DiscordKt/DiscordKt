package me.aberrantfox.kjdautils.api.annotation

import me.aberrantfox.kjdautils.internal.command.defaultPreconditionPriority

annotation class Precondition(val priority: Int = defaultPreconditionPriority)
package me.aberrantfox.kutils.api.annotations

@Target(AnnotationTarget.FUNCTION)
annotation class CommandSet(val category: String = "uncategorized")
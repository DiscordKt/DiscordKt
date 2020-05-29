package me.jakejmattson.kutils.api.annotations

@Target(AnnotationTarget.FUNCTION)
annotation class CommandSet(val category: String = "uncategorized")
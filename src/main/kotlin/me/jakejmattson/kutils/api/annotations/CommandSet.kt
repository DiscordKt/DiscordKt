package me.jakejmattson.kutils.api.annotations

/**
 * Annotates a container where commands can be created.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class CommandSet(val category: String)
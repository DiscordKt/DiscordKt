package me.jakejmattson.kutils.api.annotations

@Target(AnnotationTarget.FUNCTION)
/**
 * Creates a container where commands can be created.
 *
 * @param category The category/group that these commands will be placed into.
 */
annotation class CommandSet(val category: String)
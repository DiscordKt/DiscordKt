package me.jakejmattson.kutils.api.annotations

@Target(AnnotationTarget.CLASS)
/**
 * A class that represents some data in a JSON file.
 */
annotation class Data(val path: String, val killIfGenerated: Boolean = true)
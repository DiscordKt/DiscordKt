package me.aberrantfox.kutils.api.annotations

@Target(AnnotationTarget.CLASS)
annotation class Data(val path: String, val killIfGenerated: Boolean = true)
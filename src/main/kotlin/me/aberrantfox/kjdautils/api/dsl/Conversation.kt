package me.aberrantfox.kjdautils.api.dsl

abstract class Conversation {
    @Target(AnnotationTarget.FUNCTION)
    annotation class Start
}
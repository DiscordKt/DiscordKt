package me.aberrantfox.kutils.api.dsl.conversation

abstract class Conversation {
    @Target(AnnotationTarget.FUNCTION)
    annotation class Start
}
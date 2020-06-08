package me.jakejmattson.kutils.api.dsl.conversation

abstract class Conversation {
    @Target(AnnotationTarget.FUNCTION)
    annotation class Start
}
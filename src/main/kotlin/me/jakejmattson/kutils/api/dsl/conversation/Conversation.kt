package me.jakejmattson.kutils.api.dsl.conversation

/**
 * A class that represent a conversation.
 */
abstract class Conversation {
    /**
     * This marks the function used to start your function
     */
    @Target(AnnotationTarget.FUNCTION)
    annotation class Start
}
package me.jakejmattson.discordkt.conversations.responders

import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.create.MessageCreateBuilder

/**
 * Interface for responding to conversation messages.
 */
public interface ConversationResponder {
    /**
     * The message the user is responding to.
     */
    public val promptMessage: Message

    /**
     * The user response to the prompt.
     */
    public var userResponse: Message?

    /**
     * Dynamically respond based on the context.
     */
    public suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): ConversationResponder
}
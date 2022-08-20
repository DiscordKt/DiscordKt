package me.jakejmattson.discordkt.conversations.responders

import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.create.MessageCreateBuilder

public interface MessageResponder {
    public val ofMessage: Message
    public var userResponse: Message?

    public suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): MessageResponder
}
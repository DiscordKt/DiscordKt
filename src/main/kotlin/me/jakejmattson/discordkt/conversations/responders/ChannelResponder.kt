package me.jakejmattson.discordkt.conversations.responders

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.create.MessageCreateBuilder

/**
 * Responder for a plain text conversation.
 */
public class ChannelResponder(private val channel: MessageChannel, message: Message) : ConversationResponder {
    public override val promptMessage: Message = message
    public override var userResponse: Message? = null

    override suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): ConversationResponder {
        val newMessage = channel.createMessage {
            builder.invoke(this)
        }

        return ChannelResponder(channel, newMessage)
    }
}
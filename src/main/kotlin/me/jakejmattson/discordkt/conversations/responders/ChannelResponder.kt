package me.jakejmattson.discordkt.conversations.responders

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import kotlinx.coroutines.runBlocking

public class ChannelResponder(private val channel: MessageChannel, private val message: Message) : MessageResponder {
    public override val ofMessage: Message = message

    override suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): MessageResponder {
        val newMessage = channel.createMessage {
            builder.invoke(this)
        }

        return ChannelResponder(channel, newMessage)
    }
}
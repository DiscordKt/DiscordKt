package me.jakejmattson.discordkt.conversations.responders

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import kotlinx.coroutines.runBlocking

public class FollowupResponder(
    private val botResponse: PublicMessageInteractionResponseBehavior,
    private val followupMessage: PublicFollowupMessage? = null,
) : ConversationResponder {
    override val promptMessage: Message
        get() = runBlocking { // FIXME don't use runBlocking
            followupMessage?.message ?: getMessageOfBotResponse(botResponse.applicationId, botResponse.token)
        }

    public override var userResponse: Message? = null

    private suspend fun getMessageOfBotResponse(applicationId: Snowflake, token: String): Message {
        val messageData = botResponse.kord.rest.interaction.getInteractionResponse(applicationId, token).toData()

        return Message(messageData, botResponse.kord)
    }

    override suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): ConversationResponder {
        val newFollowupMessage = botResponse.createPublicFollowup {
            builder.invoke(this)
        }

        return FollowupResponder(botResponse, newFollowupMessage)
    }
}
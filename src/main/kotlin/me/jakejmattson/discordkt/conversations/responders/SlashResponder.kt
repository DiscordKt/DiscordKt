package me.jakejmattson.discordkt.conversations.responders

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.SlashCommandEvent

public class SlashResponder<T : TypeContainer>(private val event: SlashCommandEvent<T>) : MessageResponder {
    override val ofMessage: Message
        get() = runBlocking {
            getMessageOfBotResponse(
                event.interaction!!.applicationId,
                event.interaction!!.token
            )
        } // FIXME don't use runBlocking

    public override var userResponse: Message? = null

    private suspend fun getMessageOfBotResponse(applicationId: Snowflake, token: String): Message {
        val messageData = event.discord.kord.rest.interaction.getInteractionResponse(applicationId, token).toData()

        return Message(messageData, event.discord.kord)
    }

    override suspend fun respond(builder: suspend MessageCreateBuilder.() -> Unit): MessageResponder {
        val responseBehavior = event.interaction!!.respondPublic {
            builder.invoke(this)
        }

        return FollowupResponder(responseBehavior)
    }
}
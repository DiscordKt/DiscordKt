package me.jakejmattson.discordkt.api.conversations

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import kotlinx.coroutines.runBlocking

/**
 * Object to keep tracking of running conversations.
 */
object Conversations {
    private data class ConversationLocation(val userId: Snowflake, val channelId: Snowflake)

    private val activeConversations = mutableMapOf<ConversationLocation, Conversation>()

    @PublishedApi
    internal fun start(user: User, channel: MessageChannel, conversation: Conversation) {
        activeConversations[ConversationLocation(user.id, channel.id)] = conversation
    }

    @PublishedApi
    internal fun end(user: User, channel: MessageChannel) {
        activeConversations.remove(ConversationLocation(user.id, channel.id))
    }

    /**
     * Get a running conversation by its context, if it exists.
     */
    private fun getConversation(user: User, channel: MessageChannel) = activeConversations[ConversationLocation(user.id, channel.id)]

    /**
     * Whether or not a conversation with the given context already exists.
     */
    fun hasConversation(user: User, channel: MessageChannel) = getConversation(user, channel) != null

    internal fun handleMessage(message: Message) {
        runBlocking {
            getConversation(message.author!!, message.channel.asChannel())?.acceptMessage(message)
        }
    }

    @OptIn(KordPreview::class)
    internal fun handleInteraction(interaction: ComponentInteraction) {
        runBlocking {
            val user = interaction.kord.getUser(interaction.data.user.value!!.id)!! //TODO interaction.user.asUser() after Kord fix
            val channel = interaction.getChannel()
            getConversation(user, channel)?.acceptInteraction(interaction)
        }
    }
}
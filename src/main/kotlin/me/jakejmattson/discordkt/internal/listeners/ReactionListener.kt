package me.jakejmattson.discordkt.internal.listeners

import com.gitlab.kordlib.core.*
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import me.jakejmattson.discordkt.api.services.ConversationService

internal fun registerReactionListener(kord: Kord, conversationService: ConversationService) = kord.on<ReactionAddEvent> {
    val user = getUser().takeUnless { it.isBot ?: false } ?: return@on

    if (!conversationService.hasConversation(user, channel))
        conversationService.handleReaction(user, channel, this)
}
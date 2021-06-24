package me.jakejmattson.discordkt.internal.listeners

import dev.kord.core.Kord
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import me.jakejmattson.discordkt.api.dsl.Conversations

internal fun registerReactionListener(kord: Kord) = kord.on<ReactionAddEvent> {
    val user = getUser().takeUnless { it.isBot } ?: return@on

    if (Conversations.hasConversation(user, channel))
        Conversations.handleReaction(user, channel, this)
}
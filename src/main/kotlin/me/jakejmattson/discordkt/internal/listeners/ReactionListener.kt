package me.jakejmattson.discordkt.internal.listeners

import com.gitlab.kordlib.core.*
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import me.jakejmattson.discordkt.api.dsl.*

internal fun registerReactionListener(kord: Kord) = kord.on<ReactionAddEvent> {
    val user = getUser().takeUnless { it.isBot } ?: return@on

    if (Conversations.hasConversation(user, channel))
        Conversations.handleReaction(user, channel, this)

    handleMenuReaction(this)
}
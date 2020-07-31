package me.jakejmattson.discordkt.internal.listeners

import com.google.common.eventbus.Subscribe
import me.jakejmattson.discordkt.api.services.ConversationService
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

internal class ReactionListener(private val conversationService: ConversationService) {
    @Subscribe
    fun onReaction(event: MessageReactionAddEvent) {
        val user = event.retrieveUser().complete().takeUnless { it.isBot } ?: return

        if (!conversationService.hasConversation(user, event.channel))
            return

        conversationService.handleReaction(user, event.channel, event.reaction)
    }
}
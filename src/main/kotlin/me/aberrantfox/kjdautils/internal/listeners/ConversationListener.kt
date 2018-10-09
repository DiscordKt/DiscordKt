package me.aberrantfox.kjdautils.internal.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.internal.command.ConversationService
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent

class ConversationListener(private val conversationService: ConversationService) {
    @Subscribe
    fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (event.author.isBot) return
        if (conversationService.hasConversation(event.author.id)) conversationService.handleResponse(event.author.id, event)
    }
}

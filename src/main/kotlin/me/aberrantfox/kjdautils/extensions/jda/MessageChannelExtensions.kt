package me.aberrantfox.kjdautils.extensions.jda

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.lang.IllegalArgumentException

fun MessageChannel.messageTimed(message: String, millis: Long = 5000) = message(message) {
    if(millis < 0) {
        throw IllegalArgumentException("MessagedTimed: Delay cannot be negative.")
    }
    GlobalScope.launch {
        delay(millis)
        it.delete().queue()
    }
}

fun MessageChannel.message(message: String, action: (Message) -> Unit = {}) = sendMessage(message).queue(action)

fun MessageChannel.blockMessage(message: String) = sendMessage(message).complete()
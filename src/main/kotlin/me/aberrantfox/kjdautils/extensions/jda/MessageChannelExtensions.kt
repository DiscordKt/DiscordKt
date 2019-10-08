package me.aberrantfox.kjdautils.extensions.jda

import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.*

fun MessageChannel.messageTimed(message: String, millis: Long = 5000) = message(message) {
    require(millis >= 0) { "MessagedTimed: Delay cannot be negative." }
    GlobalScope.launch {
        delay(millis)
        it.delete().queue()
    }
}

fun MessageChannel.message(message: String, action: (Message) -> Unit = {}) = sendMessage(message).queue(action)

fun MessageChannel.blockMessage(message: String) = sendMessage(message).complete()
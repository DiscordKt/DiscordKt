package me.aberrantfox.kjdautils.extensions.jda

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import java.util.concurrent.TimeUnit

fun MessageChannel.messageTimed(message: String, delay: Long = 5000, unit: TimeUnit = TimeUnit.MILLISECONDS) = message(message) {
    require(delay >= 0) { "MessagedTimed: Delay cannot be negative." }
    it.delete().submitAfter(delay, unit)
}

fun MessageChannel.message(message: String, action: (Message) -> Unit = {}) = sendMessage(message).queue(action)

fun MessageChannel.blockMessage(message: String) = sendMessage(message).complete()
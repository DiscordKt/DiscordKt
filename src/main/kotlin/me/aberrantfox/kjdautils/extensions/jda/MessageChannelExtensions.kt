package me.aberrantfox.kjdautils.extensions.jda

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import java.util.concurrent.TimeUnit

fun MessageChannel.messageTimed(message: Any, delay: Long = 5000, unit: TimeUnit = TimeUnit.MILLISECONDS) = message(message) {
    require(delay >= 0) { "MessagedTimed: Delay cannot be negative." }
    it.delete().submitAfter(delay, unit)
}

fun MessageChannel.message(message: Any, action: (Message) -> Unit = {}) =
    when(message) {
        is String -> sendMessage(message).queue(action)
        is MessageEmbed -> sendMessage(message).queue(action)
        else -> sendMessage(message.toString()).queue(action)
    }

fun MessageChannel.blockMessage(message: String) = sendMessage(message).complete()
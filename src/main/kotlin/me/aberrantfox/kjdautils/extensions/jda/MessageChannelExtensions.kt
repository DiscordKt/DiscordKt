package me.aberrantfox.kjdautils.extensions.jda

import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel


fun MessageChannel.message(message: String, action: (Message) -> Unit = {}) = sendMessage(message).queue(action)

fun MessageChannel.blockMessage(message: String) = sendMessage(message).complete()
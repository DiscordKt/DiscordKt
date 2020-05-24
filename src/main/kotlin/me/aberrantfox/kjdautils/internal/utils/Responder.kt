package me.aberrantfox.kjdautils.internal.utils

import kotlinx.coroutines.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed

interface Responder {
    val channel: MessageChannel

    fun respond(message: String) = unsafeRespond(message.sanitiseMentions())
    fun respond(embed: MessageEmbed) = channel.sendMessage(embed).queue()
    fun respond(construct: EmbedDSLHandle.() -> Unit) = respond(embed(construct))
    fun respond(message: String, construct: EmbedDSLHandle.() -> Unit) = channel.sendMessage(message).embed(embed(construct)).queue()

    fun respondTimed(message: String, millis: Long = 5000) {
        require(millis >= 0) { "RespondTimed: Delay cannot be negative." }

        channel.sendMessage(message.sanitiseMentions()).queue {
            GlobalScope.launch {
                delay(millis)
                it.delete().queue()
            }
        }
    }

    fun respondTimed(embed: MessageEmbed, millis: Long = 5000) {
        require(millis >= 0) { "RespondTimed: Delay cannot be negative." }

        channel.sendMessage(embed).queue {
            GlobalScope.launch {
                delay(millis)
                it.delete().queue()
            }
        }
    }

    fun unsafeRespond(message: String) {
        require(message.isNotEmpty()) { "Cannot send an empty message." }
        message.chunked(2000).forEach { channel.sendMessage(it).queue() }
    }
}

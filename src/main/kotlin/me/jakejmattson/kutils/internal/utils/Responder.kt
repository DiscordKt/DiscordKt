package me.jakejmattson.kutils.internal.utils

import kotlinx.coroutines.*
import me.aberrantfox.kutils.api.dsl.embed.*
import me.aberrantfox.kutils.api.extensions.stdlib.sanitiseMentions
import net.dv8tion.jda.api.entities.*

internal interface Responder {
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

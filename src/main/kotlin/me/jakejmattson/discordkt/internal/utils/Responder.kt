@file:Suppress("unused")

package me.jakejmattson.discordkt.internal.utils

import kotlinx.coroutines.*
import me.jakejmattson.discordkt.api.dsl.embed.*
import me.jakejmattson.discordkt.api.dsl.menu.Menu
import me.jakejmattson.discordkt.api.extensions.stdlib.sanitiseMentions
import net.dv8tion.jda.api.entities.*

internal interface Responder {
    val channel: MessageChannel

    fun unsafeRespond(message: String) = chunkRespond(message)
    fun respond(message: String) = chunkRespond(message.sanitiseMentions(channel.jda))
    fun respond(embed: MessageEmbed) = channel.sendMessage(embed).queue()
    fun respond(construct: EmbedDSL.() -> Unit) = respond(embed(construct))
    fun respond(message: String, construct: EmbedDSL.() -> Unit) = channel.sendMessage(message).embed(embed(construct)).queue()
    fun respond(menu: Menu) = menu.build(channel)

    fun respondTimed(message: String, millis: Long = 5000) {
        require(millis >= 0) { "RespondTimed: Delay cannot be negative." }

        channel.sendMessage(message.sanitiseMentions(channel.jda)).queue {
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

    private fun chunkRespond(message: String) {
        require(message.isNotEmpty()) { "Cannot send an empty message." }
        message.chunked(2000).forEach { channel.sendMessage(it).queue() }
    }
}

@file:Suppress("unused")

package me.jakejmattson.discordkt.internal.utils

import com.gitlab.kordlib.core.behavior.channel.*
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.menu.Menu
import me.jakejmattson.discordkt.api.extensions.sanitiseMentions

internal interface Responder {
    val discord: Discord
    val channel: MessageChannelBehavior

    fun unsafeRespond(message: String) = runBlocking { chunkRespond(message) }
    fun respond(message: String) = runBlocking { chunkRespond(message.sanitiseMentions(discord)) }
    fun respond(construct: EmbedBuilder.() -> Unit) = runBlocking { channel.createEmbed { construct.invoke(this) } }
    fun respond(message: String, construct: EmbedBuilder.() -> Unit) = runBlocking {
        channel.createMessage {
            content = message
            construct.invoke(embed!!)
        }
    }

    fun respond(menu: Menu) = runBlocking { menu.build(channel) }

    private suspend fun chunkRespond(message: String) {
        require(message.isNotEmpty()) { "Cannot send an empty message." }
        message.chunked(2000).forEach { channel.createMessage(it) }
    }
}

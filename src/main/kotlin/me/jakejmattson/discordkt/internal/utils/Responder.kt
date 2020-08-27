@file:Suppress("unused")

package me.jakejmattson.discordkt.internal.utils

import com.gitlab.kordlib.core.behavior.channel.*
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.menu.Menu
import me.jakejmattson.discordkt.api.extensions.sanitiseMentions

internal interface Responder {
    val discord: Discord
    val channel: MessageChannelBehavior

    suspend fun unsafeRespond(message: String) = chunkRespond(message)
    suspend fun respond(message: String) = chunkRespond(message.sanitiseMentions(discord))
    suspend fun respond(construct: suspend EmbedBuilder.() -> Unit) = channel.createEmbed { construct.invoke(this) }
    suspend fun respond(message: String, construct: suspend EmbedBuilder.() -> Unit) =
        channel.createMessage {
            content = message
            construct.invoke(embed!!)
        }

    suspend fun respond(menu: Menu) = menu.build(channel)

    private suspend fun chunkRespond(message: String) {
        require(message.isNotEmpty()) { "Cannot send an empty message." }
        message.chunked(2000).forEach { channel.createMessage(it) }
    }
}

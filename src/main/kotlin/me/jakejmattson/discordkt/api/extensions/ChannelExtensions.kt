@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.createTextChannel
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.rest.builder.channel.TextChannelCreateBuilder
import dev.kord.rest.builder.channel.VoiceChannelCreateBuilder
import me.jakejmattson.discordkt.api.dsl.MenuBuilder

/**
 * Create a new text channel under this category.
 */
suspend fun Category.createTextChannel(name: String, builder: TextChannelCreateBuilder.() -> Unit = {}) = getGuild().createTextChannel(name) {
    parentId = id
    builder.invoke(this)
}

/**
 * Create a new voice channel under this category.
 */
suspend fun Category.createVoiceChannel(name: String, builder: VoiceChannelCreateBuilder.() -> Unit = {}) = getGuild().createVoiceChannel(name) {
    parentId = id
    builder.invoke(this)
}

/**
 * Move this member to a new voice channel.
 */
suspend fun VoiceState.move(channel: VoiceChannel) = getMember().edit { voiceChannelId = channel.id }

/**
 * Disconnect this member from their voice channel.
 */
suspend fun VoiceState.disconnect() = getMember().edit { voiceChannelId = null }

/**
 * Create a new menu in a message channel.
 */
suspend fun MessageChannelBehavior.createMenu(construct: suspend MenuBuilder.() -> Unit): Message? {
    val handle = MenuBuilder()
    handle.construct()
    return handle.build().send(this)
}

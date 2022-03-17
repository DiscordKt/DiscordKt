@file:Suppress("unused")

package me.jakejmattson.discordkt.extensions

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.createTextChannel
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.rest.builder.channel.TextChannelCreateBuilder
import dev.kord.rest.builder.channel.VoiceChannelCreateBuilder
import me.jakejmattson.discordkt.dsl.MenuBuilder

/**
 * Create a new [TextChannel] under this [Category].
 */
public suspend fun Category.createTextChannel(name: String, builder: TextChannelCreateBuilder.() -> Unit = {}): TextChannel = getGuild().createTextChannel(name) {
    parentId = id
    builder.invoke(this)
}

/**
 * Create a new [VoiceChannel] under this [Category].
 */
public suspend fun Category.createVoiceChannel(name: String, builder: VoiceChannelCreateBuilder.() -> Unit = {}): VoiceChannel = getGuild().createVoiceChannel(name) {
    parentId = id
    builder.invoke(this)
}

/**
 * Move this [Member] to a new voice channel.
 */
public suspend fun VoiceState.move(channel: VoiceChannel): Member = getMember().edit { voiceChannelId = channel.id }

/**
 * Disconnect this [Member] from their [VoiceChannel].
 */
public suspend fun VoiceState.disconnect(): Member = getMember().edit { voiceChannelId = null }

/**
 * Create a new menu in a message channel.
 */
public suspend fun MessageChannelBehavior.createMenu(construct: suspend MenuBuilder.() -> Unit): Message {
    val handle = MenuBuilder()
    handle.construct()
    return handle.build().send(this)
}

package me.jakejmattson.discordkt.api.extensions

import com.gitlab.kordlib.core.behavior.*
import com.gitlab.kordlib.core.entity.VoiceState
import com.gitlab.kordlib.core.entity.channel.*
import com.gitlab.kordlib.rest.builder.channel.*

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

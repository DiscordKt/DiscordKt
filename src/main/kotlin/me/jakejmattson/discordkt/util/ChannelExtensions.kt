@file:Suppress("unused")

package me.jakejmattson.discordkt.util

import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.channel.VoiceChannel
import me.jakejmattson.discordkt.dsl.MenuBuilder

/**
 * Move this [Member] to a new [VoiceChannel].
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

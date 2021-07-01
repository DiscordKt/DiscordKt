@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.ReactionEmoji
import dev.kord.gateway.Intent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import java.util.*

/**
 * Convert a Long ID to a [Snowflake].
 */
fun Long.toSnowflake() = Snowflake(this)

/**
 * Convert a String ID or mention to a [Snowflake].
 */
fun String.toSnowflake() = Snowflake(this)

/**
 * Convert an ID or mention to a [Snowflake].
 */
fun String.toSnowflakeOrNull() = trimToID().toLongOrNull()?.let { Snowflake(it) }

/**
 * Combine two [Intent] into a set.
 */
operator fun Intent.plus(intent: Intent) = mutableSetOf(this, intent)

/**
 * Convert a [DiscordEmoji] to a [DiscordPartialEmoji].
 */
fun DiscordEmoji.toPartialEmoji() = toReaction().toPartialEmoji()

/**
 * Convert a [ReactionEmoji] to a [DiscordPartialEmoji].
 */
fun ReactionEmoji.toPartialEmoji() = DiscordPartialEmoji(name = this.name)

/**
 * Create an interaction button with a [UUID].
 */
@OptIn(KordPreview::class)
fun ActionRowBuilder.button(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: ButtonBuilder.InteractionButtonBuilder.() -> Unit) {
    interactionButton(style, UUID.randomUUID().toString()) {
        this.label = label
        this.emoji = emoji?.toPartialEmoji()
        this.disabled = disabled

        action.invoke(this)
    }
}
@file:Suppress("unused")

package me.jakejmattson.discordkt.extensions

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.enableEvent
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.Event
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import java.util.*

/**
 * Convert a Long ID to a [Snowflake].
 */
public fun Long.toSnowflake(): Snowflake = Snowflake(this)

/**
 * Convert a String ID or mention to a [Snowflake].
 */
public fun String.toSnowflake(): Snowflake = Snowflake(this)

/**
 * Convert an ID or mention to a [Snowflake].
 */
public fun String.toSnowflakeOrNull(): Snowflake? = trimToID().toLongOrNull()?.let { Snowflake(it) }

/**
 * Combine two [Intent] into [Intents].
 */
public operator fun Intent.plus(intent: Intent): Intents = Intents(this, intent)

/**
 * Convert an [Event] to its [Intents].
 */
public inline fun <reified T : Event> intentsOf(): Intents = Intents { enableEvent<T>() }

/**
 * Convert a [DiscordEmoji] to a [DiscordPartialEmoji].
 */
public fun DiscordEmoji.toPartialEmoji(): DiscordPartialEmoji = toReaction().toPartialEmoji()

/**
 * Convert a [ReactionEmoji] to a [DiscordPartialEmoji].
 */
public fun ReactionEmoji.toPartialEmoji(): DiscordPartialEmoji = DiscordPartialEmoji(name = this.name)

/**
 * Create an interaction button with a [UUID].
 */
@OptIn(KordPreview::class)
public fun ActionRowBuilder.button(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: ButtonBuilder.InteractionButtonBuilder.() -> Unit) {
    interactionButton(style, UUID.randomUUID().toString()) {
        this.label = label
        this.emoji = emoji?.toPartialEmoji()
        this.disabled = disabled

        action.invoke(this)
    }
}
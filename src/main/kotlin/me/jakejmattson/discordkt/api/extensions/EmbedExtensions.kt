@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import dev.kord.rest.builder.message.EmbedBuilder

/**
 * Add a field to an embed. Shorthand for the equivalent builder.
 */
public fun EmbedBuilder.addField(name: String, value: String): Unit = field {
    this.name = name.validOrBlank()
    this.value = value.validOrBlank()
    this.inline = false
}

/**
 * Add an inline field to an embed. Shorthand for the equivalent builder.
 */
public fun EmbedBuilder.addInlineField(name: String, value: String): Unit = field {
    this.name = name.validOrBlank()
    this.value = value.validOrBlank()
    this.inline = true
}

private fun String.validOrBlank() = ifBlank { "\u200E" } //Zero-width Space
package me.jakejmattson.discordkt.api.extensions

import com.gitlab.kordlib.rest.builder.message.EmbedBuilder

/**
 * Utility function to create a field.
 */
fun EmbedBuilder.addField(name: String, value: String) = field {
    this.name = name
    this.value = value
}

/**
 * Utility function to create an inline field.
 */
fun EmbedBuilder.addInlineField(name: String, value: String) = field {
    this.name = name
    this.value = value
    this.inline = true
}
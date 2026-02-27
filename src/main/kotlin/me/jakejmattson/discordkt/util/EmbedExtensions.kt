package me.jakejmattson.discordkt.util

import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder

/**
 * Create an author field from the given [User]. Set's the name, icon, and url.
 */
public fun EmbedBuilder.author(user: User) {
    author {
        this.name = user.fullName
        this.icon = user.pfpUrl
        this.url = user.profileLink
    }
}

/**
 * Set the thumbnail of an embed given a URL.
 */
public fun EmbedBuilder.thumbnail(url: String) {
    thumbnail {
        this.url = url
    }
}

/**
 * Set the footer of an embed given its text and optional icon.
 */
public fun EmbedBuilder.footer(text: String, icon: String? = null) {
    footer {
        this.text = text
        this.icon = icon
    }
}

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
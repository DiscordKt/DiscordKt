package me.jakejmattson.discordkt.api.extensions

import com.gitlab.kordlib.rest.builder.message.EmbedBuilder

fun EmbedBuilder.addField(name: String, value: String) = field {
    this.name = name
    this.value = value
}

fun EmbedBuilder.addInlineField(name: String, value: String) = field {
    this.name = name
    this.value = value
    this.inline = true
}
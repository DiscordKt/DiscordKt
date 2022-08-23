package me.jakejmattson.discordkt.prompts

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL

internal data class SelectOption(val label: String, val value: String, val description: String?, val emoji: DiscordPartialEmoji?)

public class SimpleSelectBuilder {
    internal val options: MutableList<SelectOption> = mutableListOf()
    internal var textContent: String? = null
    internal var embedContent: (EmbedBuilder.() -> Unit)? = null
    public var selectionCount: IntRange = 1..1

    @BuilderDSL
    public fun content(text: String? = null, embed: (EmbedBuilder.() -> Unit)? = null) {
        textContent = text
        embedContent = embed
    }

    @BuilderDSL
    public fun option(label: String, value: String = label, description: String? = null, emoji: DiscordPartialEmoji? = null) {
        options.add(SelectOption(label, value, description, emoji))
    }
}
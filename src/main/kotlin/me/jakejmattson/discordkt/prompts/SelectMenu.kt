package me.jakejmattson.discordkt.prompts

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL

/**
 * A single option in a select menu.
 *
 * @param label The text displayed on Discord.
 * @param value The value returned on selection.
 * @param description The text show under the label.
 * @param emoji The emoji shown next to the option.
 */
internal data class SelectOption(val label: String, val value: String, val description: String?, val emoji: DiscordPartialEmoji?)

/**
 * A simplified builder for a select menu.
 */
public class SimpleSelectBuilder {
    internal val options: MutableList<SelectOption> = mutableListOf()
    internal var textContent: String? = null
    internal var embedContent: (EmbedBuilder.() -> Unit)? = null

    /** The number of selections allowed */
    public var selectionCount: IntRange = 1..1

    /**
     * Set the content of the message above the menu.
     *
     * @param text The text content of the message.
     * @param embed The embed content of the message.
     */
    @BuilderDSL
    public fun content(text: String? = null, embed: (EmbedBuilder.() -> Unit)? = null) {
        textContent = text
        embedContent = embed
    }

    /**
     * Create an option in a select menu.
     *
     * @param label The text displayed on Discord.
     * @param value The value returned on selection.
     * @param description The text show under the label.
     * @param emoji The emoji shown next to the option.
     */
    @BuilderDSL
    public fun option(label: String, value: String = label, description: String? = null, emoji: DiscordPartialEmoji? = null) {
        options.add(SelectOption(label, value, description, emoji))
    }
}
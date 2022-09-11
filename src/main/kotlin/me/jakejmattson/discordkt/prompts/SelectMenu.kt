package me.jakejmattson.discordkt.prompts

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import me.jakejmattson.discordkt.Args2
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.util.uuid

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

internal val selectBuffer = Channel<SelectMenuInteraction>()

/**
 * Create a discord select menu.
 */
public suspend fun promptSelect(interaction: ApplicationCommandInteraction, builder: SimpleSelectBuilder.() -> Unit): Args2<DeferredEphemeralMessageInteractionResponseBehavior, List<String>> {
    val id = uuid()
    val selectBuilder = SimpleSelectBuilder()
    selectBuilder.builder()

    interaction.respondEphemeral {
        content = selectBuilder.textContent
        selectBuilder.embedContent?.let { embed { it.invoke(this) } }

        actionRow {
            selectMenu(id) {
                this.allowedValues = selectBuilder.selectionCount

                selectBuilder.options.forEach {
                    option(it.label, it.value) {
                        this.description = it.description
                        this.emoji = it.emoji
                    }
                }
            }
        }
    }

    return retrieveValidModalResponse(id)
}

private fun retrieveValidModalResponse(modalId: String): Args2<DeferredEphemeralMessageInteractionResponseBehavior, List<String>> = runBlocking {
    retrieveModalResponse(modalId) ?: retrieveValidModalResponse(modalId)
}

private suspend fun retrieveModalResponse(selectId: String): Args2<DeferredEphemeralMessageInteractionResponseBehavior, List<String>>? = select {
    selectBuffer.onReceive { interaction ->
        if (interaction.componentId != selectId) return@onReceive null
        Args2(interaction.deferEphemeralResponse(), interaction.values)
    }
}
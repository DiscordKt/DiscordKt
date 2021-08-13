@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.api.extensions.toPartialEmoji
import me.jakejmattson.discordkt.internal.utils.InternalLogger
import java.util.*

private val menus = mutableMapOf<Snowflake, Menu>()

/**
 * Builder functions for menu buttons.
 */
class MenuButtonRowBuilder {
    internal val buttons = mutableListOf<DktButton>()

    /**
     * A Discord button component.
     * Exposes the menu for navigation functions.
     *
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param style The Button [style][ButtonStyle]
     * @param disabled Whether this button is disabled
     */
    @OptIn(KordPreview::class)
    fun button(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: suspend Menu.() -> Unit) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, UUID.randomUUID().toString(), Nav(action), style)
        buttons.add(button)
    }

    /**
     * A Discord button component.
     * Exposes the current embed page to be edited.
     *
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param style The Button [style][ButtonStyle]
     * @param disabled Whether this button is disabled
     */
    @OptIn(KordPreview::class)
    fun editButton(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: suspend EmbedBuilder.() -> Unit) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, UUID.randomUUID().toString(), Edit(action), style)
        buttons.add(button)
    }

    /**
     * A Discord button component.
     * Exposes the button press interaction.
     *
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param style The Button [style][ButtonStyle]
     * @param disabled Whether this button is disabled
     */
    @OptIn(KordPreview::class)
    fun actionButton(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: suspend ComponentInteraction.() -> Unit) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, UUID.randomUUID().toString(), Action(action), style)
        buttons.add(button)
    }

    /**
     * A Discord link button component.
     * Opens the specified link in the browser.
     *
     * @param url The link this button will open.
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param disabled Whether this button is disabled
     */
    fun linkButton(label: String?, emoji: DiscordEmoji?, url: String, disabled: Boolean = false) {
        val button = LinkButton(label, emoji?.toReaction(), disabled, url)
        buttons.add(button)
    }
}

/**
 * Type-safe builder for creating paginated embeds with button components.
 */
class MenuBuilder {
    private val pages = mutableListOf<EmbedBuilder>()
    private val componentRows = mutableListOf<MutableList<DktButton>>()

    /**
     * Add a new page to this menu using an EmbedBuilder.
     */
    suspend fun page(construct: suspend EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
        construct.invoke(embed)
        pages.add(embed)
    }

    /**
     * Create a new row of buttons using the [button][MenuButtonRowBuilder.editButton] or [linkButton][MenuButtonRowBuilder.linkButton] builders.
     */
    fun buttons(rowBuilder: MenuButtonRowBuilder.() -> Unit) {
        val builder = MenuButtonRowBuilder()
        rowBuilder.invoke(builder)
        componentRows.add(builder.buttons)
    }

    internal fun build() = Menu(pages, componentRows)
}

/**
 * Contains menu data and navigation functions.
 */
data class Menu(internal val pages: MutableList<EmbedBuilder>,
                internal val buttons: MutableList<MutableList<DktButton>>) {
    private var index = 0

    internal val page: EmbedBuilder
        get() = pages[index]

    /**
     * Navigate to the previous page (wraps)
     */
    fun previousPage() = navigate(-1)

    /**
     * Navigate to the next page (wraps)
     */
    fun nextPage() = navigate(1)

    /**
     * Load a specific page by index
     *
     * @param page The index of the desired page
     */
    fun loadPage(page: Int): EmbedBuilder {
        if (page !in pages.indices)
            throw IllegalArgumentException("Invalid page index $page for ${pages.size} pages.")

        index = page
        return pages[index]
    }

    internal fun updatePage(embed: EmbedBuilder) {
        pages[index] = embed
    }

    private fun navigate(direction: Int): EmbedBuilder {
        index += direction

        index = when {
            index > pages.lastIndex -> 0
            index < 0 -> pages.lastIndex
            else -> index
        }

        return pages[index]
    }

    @OptIn(KordPreview::class)
    internal suspend fun send(channel: MessageChannelBehavior): Message? {
        if (pages.isEmpty()) {
            InternalLogger.error("A menu must have at least one page.")
            return null
        }

        val message = channel.createMessage {
            embeds.add(pages.first())

            buttons.forEach {
                actionRow {
                    it.forEach { button ->
                        when (button) {
                            is SimpleButton<*> -> {
                                interactionButton(button.style, button.id) {
                                    this.label = button.label
                                    this.emoji = button.emoji?.toPartialEmoji()
                                }
                            }
                            is LinkButton -> {
                                linkButton(button.url) {
                                    this.label = button.label
                                    this.emoji = button.emoji?.toPartialEmoji()
                                }
                            }
                        }
                    }
                }
            }
        }

        menus[message.id] = this
        return message
    }

    internal companion object {
        @OptIn(KordPreview::class)
        internal suspend fun handleButtonPress(interaction: ComponentInteraction) {
            val firedButton = interaction.component ?: return

            if (firedButton.data.url.value != null)
                return

            val message = interaction.message!!
            val menu = menus[message.id] ?: return
            val simpleButtons = menu.buttons.flatten().filterIsInstance<SimpleButton<*>>()
            val simpleButton = simpleButtons.find { firedButton.data.customId.value == it.id } ?: return

            when (val action = simpleButton.action) {
                is Nav -> {
                    interaction.acknowledgeEphemeralDeferredMessageUpdate()
                    action.invoke(menu)

                    message.edit {
                        embeds = mutableListOf(menu.page)
                    }
                }
                is Edit -> {
                    interaction.acknowledgeEphemeralDeferredMessageUpdate()
                    val page = menu.page
                    action.invoke(page)
                    menu.updatePage(page)

                    message.edit {
                        embeds = mutableListOf(page)
                    }
                }
                is Action -> action.invoke(interaction)
                else -> return
            }
        }
    }
}

/**
 * Used internally to represent a button.
 *
 * @property label Optional button text
 * @property emoji Optional button emoji
 * @property disabled Whether the button is disabled.
 */
interface DktButton {
    val label: String?
    val emoji: ReactionEmoji?
    var disabled: Boolean
}

private class SimpleButton<T>(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    val id: String,
    val action: ButtonAction<T>,

    @OptIn(KordPreview::class)
    val style: ButtonStyle
) : DktButton

private sealed class ButtonAction<T> {
    abstract val action: suspend T.() -> Unit

    suspend fun invoke(data: T) {
        action.invoke(data)
    }
}

@OptIn(KordPreview::class)
private class Action(override val action: suspend ComponentInteraction.() -> Unit) : ButtonAction<ComponentInteraction>()
private class Edit(override val action: suspend EmbedBuilder.() -> Unit) : ButtonAction<EmbedBuilder>()
private class Nav(override val action: suspend Menu.() -> Unit) : ButtonAction<Menu>()

private data class LinkButton @OptIn(KordPreview::class) constructor(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    val url: String,
) : DktButton
@file:Suppress("unused")

package me.jakejmattson.discordkt.dsl

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.util.toPartialEmoji
import me.jakejmattson.discordkt.util.uuid

private val menus = mutableMapOf<Snowflake, Menu>()

/**
 * Replace an existing message with a [Menu].
 */
public suspend fun Message.edit(menu: Menu): Message {
    val message = edit {
        content = null
        components?.clear()
        embeds = mutableListOf(menu.page)

        menu.buttons.forEach {
            actionRow {
                it.forEach { button ->
                    when (button) {
                        is SimpleButton -> interactionButton(button.style, button.id) {
                            label = button.label
                            emoji = button.emoji?.toPartialEmoji()
                            disabled = button.disabled
                        }

                        is LinkButton -> linkButton(button.url) {
                            label = button.label
                            emoji = button.emoji?.toPartialEmoji()
                            disabled = button.disabled
                        }
                    }
                }
            }
        }
    }

    menus[id] = menu
    return message
}

/**
 * Builder functions for menu buttons.
 */
public class MenuButtonRowBuilder {
    internal val buttons = mutableListOf<DktButton>()

    /**
     * A Discord button component.
     * Exposes the menu for navigation functions.
     *
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param style The Button [style][ButtonStyle]
     * @param disabled Whether this button is disabled
     * @param bypassDefer If true, do not defer interaction
     * @param action Lambda to be executed when button is pressed
     */
    public fun button(
        label: String?,
        emoji: DiscordEmoji?,
        style: ButtonStyle = ButtonStyle.Secondary,
        disabled: Boolean = false,
        bypassDefer: Boolean = false,
        action: suspend Menu.() -> Unit
    ) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, uuid(), Nav(action), style, bypassDefer)
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
     * @param bypassDefer If true, do not defer interaction
     * @param action Lambda to be executed when button is pressed
     */
    public fun editButton(
        label: String?,
        emoji: DiscordEmoji?,
        style: ButtonStyle = ButtonStyle.Secondary,
        disabled: Boolean = false,
        bypassDefer: Boolean = false,
        action: suspend EmbedBuilder.(ButtonInteraction) -> Unit
    ) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, uuid(), Edit(action), style, bypassDefer)
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
    public fun linkButton(label: String?, emoji: DiscordEmoji?, url: String, disabled: Boolean = false) {
        val button = LinkButton(label, emoji?.toReaction(), disabled, url)
        buttons.add(button)
    }
}

/**
 * Type-safe builder for creating paginated embeds with button components.
 */
public class MenuBuilder {
    private val pages = mutableListOf<EmbedBuilder>()
    private val componentRows = mutableListOf<MutableList<DktButton>>()

    /**
     * Add a new page to this menu using an EmbedBuilder.
     */
    public suspend fun page(construct: suspend EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
        construct.invoke(embed)
        pages.add(embed)
    }

    /**
     * Create a new row of buttons using the [button][MenuButtonRowBuilder.editButton] or [linkButton][MenuButtonRowBuilder.linkButton] builders.
     */
    public fun buttons(rowBuilder: MenuButtonRowBuilder.() -> Unit) {
        val builder = MenuButtonRowBuilder()
        rowBuilder.invoke(builder)
        componentRows.add(builder.buttons)
    }

    internal fun build() = Menu(pages, componentRows)
}

/**
 * Create a [Menu]
 */
@BuilderDSL
public suspend fun menu(menuBuilder: suspend MenuBuilder.() -> Unit): Menu {
    val handle = MenuBuilder()
    handle.menuBuilder()
    return handle.build()
}

/**
 * Contains menu data and navigation functions.
 */
public data class Menu(
    internal val pages: MutableList<EmbedBuilder>,
    internal val buttons: MutableList<MutableList<DktButton>>
) {
    private var index = 0

    internal val page: EmbedBuilder
        get() = pages[index]

    /**
     * Navigate to the previous page (wraps)
     */
    public fun previousPage(): EmbedBuilder = navigate(-1)

    /**
     * Navigate to the next page (wraps)
     */
    public fun nextPage(): EmbedBuilder = navigate(1)

    /**
     * Load a specific page by index
     *
     * @param page The index of the desired page
     */
    public fun loadPage(page: Int): EmbedBuilder {
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

    internal suspend fun send(channel: MessageChannelBehavior): Message {
        require(pages.isNotEmpty()) { "A menu must have at least one page." }

        val message = channel.createMessage {
            embeds = mutableListOf(pages.first())

            buttons.forEach {
                actionRow {
                    it.forEach { button ->
                        when (button) {
                            is SimpleButton -> interactionButton(button.style, button.id) {
                                this.label = button.label
                                this.emoji = button.emoji?.toPartialEmoji()
                                this.disabled = button.disabled
                            }

                            is LinkButton -> linkButton(button.url) {
                                this.label = button.label
                                this.emoji = button.emoji?.toPartialEmoji()
                                this.disabled = button.disabled
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
        internal suspend fun handleButtonPress(interaction: ButtonInteraction) {
            val firedButton = interaction.component

            if (firedButton.data.url.value != null)
                return

            val message = interaction.message
            val menu = menus[message.id] ?: return
            val simpleButtons = menu.buttons.flatten().filterIsInstance<SimpleButton>()
            val simpleButton = simpleButtons.find { firedButton.data.customId.value == it.id } ?: return

            when (val actionButton = simpleButton.actionButton) {
                is Nav -> {
                    if (!simpleButton.bypassDefer) interaction.deferEphemeralMessageUpdate()
                    actionButton.action.invoke(menu)

                    message.edit {
                        embeds = mutableListOf(menu.page)
                    }
                }

                is Edit -> {
                    if (!simpleButton.bypassDefer) interaction.deferEphemeralMessageUpdate()
                    val page = menu.page
                    actionButton.action.invoke(page, interaction)
                    menu.updatePage(page)

                    message.edit {
                        embeds = mutableListOf(page)
                    }
                }
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
public interface DktButton {
    public val label: String?
    public val emoji: ReactionEmoji?
    public var disabled: Boolean
}

private class SimpleButton(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    val id: String,
    val actionButton: ActionButton,
    val style: ButtonStyle,
    val bypassDefer: Boolean = false
) : DktButton

private sealed interface ActionButton
private class Edit(val action: suspend EmbedBuilder.(ButtonInteraction) -> Unit) : ActionButton
private class Nav(val action: suspend Menu.() -> Unit) : ActionButton

private data class LinkButton(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    val url: String,
) : DktButton
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
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.modify.actionRow
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
                        is SimpleButton<*> -> interactionButton(button.style, button.id) {
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
     */
    public fun button(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: suspend Menu.() -> Unit) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, uuid(), Nav(action), style)
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
    public fun editButton(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: suspend EmbedBuilder.() -> Unit) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, uuid(), Edit(action), style)
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
    public fun actionButton(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: suspend ComponentInteraction.() -> Unit) {
        val button = SimpleButton(label, emoji?.toReaction(), disabled, uuid(), Action(action), style)
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
public data class Menu(internal val pages: MutableList<EmbedBuilder>,
                       internal val buttons: MutableList<MutableList<DktButton>>) {
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
            embeds.add(pages.first())

            buttons.forEach {
                actionRow {
                    it.forEach { button ->
                        when (button) {
                            is SimpleButton<*> -> interactionButton(button.style, button.id) {
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
            val simpleButtons = menu.buttons.flatten().filterIsInstance<SimpleButton<*>>()
            val simpleButton = simpleButtons.find { firedButton.data.customId.value == it.id } ?: return

            when (val action = simpleButton.action) {
                is Nav -> {
                    interaction.deferEphemeralMessageUpdate()
                    action.invoke(menu)

                    message.edit {
                        embeds = mutableListOf(menu.page)
                    }
                }

                is Edit -> {
                    interaction.deferEphemeralMessageUpdate()
                    val page = menu.page
                    action.invoke(page)
                    menu.updatePage(page)

                    message.edit {
                        embeds = mutableListOf(page)
                    }
                }

                is Action -> action.invoke(interaction)
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

private class SimpleButton<T>(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    val id: String,
    val action: ButtonAction<T>,
    val style: ButtonStyle
) : DktButton

private sealed class ButtonAction<T> {
    abstract val action: suspend T.() -> Unit

    suspend fun invoke(data: T) {
        action.invoke(data)
    }
}

private class Action(override val action: suspend ComponentInteraction.() -> Unit) : ButtonAction<ComponentInteraction>()
private class Edit(override val action: suspend EmbedBuilder.() -> Unit) : ButtonAction<EmbedBuilder>()
private class Nav(override val action: suspend Menu.() -> Unit) : ButtonAction<Menu>()

private data class LinkButton constructor(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    val url: String,
) : DktButton
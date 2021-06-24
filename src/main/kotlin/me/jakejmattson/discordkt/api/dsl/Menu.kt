@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.*
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.internal.utils.InternalLogger
import java.util.*

private val menus = mutableMapOf<Snowflake, Menu>()

class ButtonRowBuilder {
    internal val buttons = mutableListOf<DktButton>()

    /**
     * A Discord button component.
     * Exposes the menu for navigation functions.
     *
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param style The Button [style][ButtonStyle]
     * @param disabled Whether or not this button is disabled
     */
    @OptIn(KordPreview::class)
    fun button(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: Menu.() -> Unit) {
        val button = NavButton(label, emoji?.toReaction(), disabled, style, UUID.randomUUID().toString(), action)
        buttons.add(button)
    }

    /**
     * A Discord button component.
     * Exposes the current embed page to be edited.
     *
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param style The Button [style][ButtonStyle]
     * @param disabled Whether or not this button is disabled
     */
    @OptIn(KordPreview::class)
    fun editButton(label: String?, emoji: DiscordEmoji?, style: ButtonStyle = ButtonStyle.Secondary, disabled: Boolean = false, action: EmbedBuilder.() -> Unit) {
        val button = EditButton(label, emoji?.toReaction(), disabled, style, UUID.randomUUID().toString(), action)
        buttons.add(button)
    }

    /**
     * A Discord link button component.
     * Opens the specified link in the browser.
     *
     * @param url The link this button will open.
     * @param label The Button text
     * @param emoji The Button [emoji][DiscordEmoji]
     * @param disabled Whether or not this button is disabled
     */
    fun linkButton(url: String, label: String?, emoji: DiscordEmoji?, disabled: Boolean = false) {
        val button = LinkButton(label, emoji?.toReaction(), disabled, url)
        buttons.add(button)
    }
}

/**
 * Type-safe builder for creating paginated embeds with custom reactions.
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
     * Create a new row of buttons using the [button][ButtonRowBuilder.editButton] or [linkButton][ButtonRowBuilder.linkButton] builders.
     */
    fun buttons(rowBuilder: ButtonRowBuilder.() -> Unit) {
        val builder = ButtonRowBuilder()
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
        if (channel.asChannel().type == ChannelType.DM) {
            InternalLogger.error("Cannot use menus within a private context.")
            return null
        }

        if (pages.isEmpty()) {
            InternalLogger.error("A menu must have at least one page.")
            return null
        }

        val message = channel.createMessage {
            embed = pages.first()

            buttons.forEach {
                actionRow {
                    it.forEach { button ->
                        when (button) {
                            is SimpleButton -> {
                                interactionButton(button.style, button.id) {
                                    this.label = button.label
                                    this.emoji = DiscordPartialEmoji(name = button.emoji?.name)
                                }
                            }
                            is LinkButton -> {
                                linkButton(button.url) {
                                    this.label = button.label
                                    this.emoji = DiscordPartialEmoji(name = button.emoji?.name)
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
}

@OptIn(KordPreview::class)
internal suspend fun handleButtonPress(interaction: ComponentInteraction) {
    interaction.acknowledgeEphemeralDeferredMessageUpdate()
    val firedButton = interaction.component ?: return

    if (firedButton.url != null)
        return

    val message = interaction.message!!
    val menu = menus[message.id] ?: return
    val simpleButtons = menu.buttons.flatten().filterIsInstance<SimpleButton>()

    val newEmbed = when (val simpleButton = simpleButtons.find { firedButton.customId == it.id }) {
        is NavButton -> {
            simpleButton.action.invoke(menu)
            menu.page
        }
        is EditButton -> {
            val page = menu.page
            simpleButton.action.invoke(page)
            menu.updatePage(page)
            page
        }
        else -> return
    }

    message.edit {
        embed = newEmbed
    }
}

interface DktButton {
    val label: String?
    val emoji: ReactionEmoji?
    var disabled: Boolean
}

internal interface SimpleButton : DktButton {
    override val label: String?
    override val emoji: ReactionEmoji?
    override var disabled: Boolean
    val id: String

    @OptIn(KordPreview::class)
    val style: ButtonStyle
}

internal data class NavButton @OptIn(KordPreview::class) constructor(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    override val style: ButtonStyle,
    override val id: String,
    val action: Menu.() -> Unit
) : SimpleButton

internal data class EditButton @OptIn(KordPreview::class) constructor(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    override val style: ButtonStyle,
    override val id: String,
    val action: EmbedBuilder.() -> Unit
) : SimpleButton

internal data class LinkButton @OptIn(KordPreview::class) constructor(
    override val label: String?,
    override val emoji: ReactionEmoji?,
    override var disabled: Boolean,
    val url: String,
) : DktButton
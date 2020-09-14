@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl

import com.gitlab.kordlib.common.entity.*
import com.gitlab.kordlib.core.behavior.channel.*
import com.gitlab.kordlib.core.behavior.edit
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.kordx.emoji.*
import com.gitlab.kordlib.kordx.emoji.DiscordEmoji
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.internal.utils.InternalLogger

private val menus = mutableMapOf<Snowflake, Menu>()

/**
 * Type-safe builder for creating paginated embeds with custom reactions.
 *
 * @property leftReact Reaction used to move to the previous page.
 * @property rightReact Reaction used to move to the next page.
 */
class MenuBuilder {
    private val pages = mutableListOf<EmbedBuilder>()
    private val reactions = mutableMapOf<DiscordEmoji, suspend EmbedBuilder.() -> Unit>()
    var leftReact: DiscordEmoji = Emojis.arrowLeft
    var rightReact: DiscordEmoji = Emojis.arrowRight

    /**
     * Add a new page to this menu using an EmbedBuilder.
     */
    suspend fun page(construct: suspend EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
        construct.invoke(embed)
        pages.add(embed)
    }

    /**
     * Add a reaction to the menu and the action to execute when it is clicked.
     */
    fun reaction(reaction: DiscordEmoji, action: suspend EmbedBuilder.() -> Unit) {
        reactions[reaction] = action
    }

    internal fun build() = Menu(pages, reactions, leftReact.toReaction(), rightReact.toReaction())
}

/**
 * Class for storing menu data.
 *
 * @property pages A list of embed representing menu pages.
 * @property leftReact Reaction used to move to the previous page.
 * @property rightReact Reaction used to move to the next page.
 * @property customReactions All custom reactions and their actions.
 */
data class Menu(private val pages: MutableList<EmbedBuilder>,
                val customReactions: Map<DiscordEmoji, suspend EmbedBuilder.() -> Unit>,
                val leftReact: ReactionEmoji,
                val rightReact: ReactionEmoji) {
    private var index = 0

    internal fun previousPage() = navigate(-1)
    internal fun nextPage() = navigate(1)

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

    internal suspend fun send(channel: MessageChannelBehavior) {
        if (channel.asChannel().type == ChannelType.DM)
            return InternalLogger.error("Cannot use menus within a private context.")

        if (pages.isEmpty())
            return InternalLogger.error("A menu must have at least one page.")

        val message = channel.createMessage {
            embed = pages.first()
        }

        val isMultiPage = pages.size != 1

        if (isMultiPage) {
            message.addReaction(leftReact)
            message.addReaction(rightReact)
        }

        customReactions.keys.forEach { message.addReaction(it) }

        if (isMultiPage || customReactions.isNotEmpty())
            menus[message.id] = this
    }
}

internal suspend fun handleMenuReaction(event: ReactionAddEvent) = with(event) {
    val menu = menus[messageId] ?: return@with

    message.deleteReaction(user.id, emoji)

    val newEmbed = when (emoji) {
        menu.leftReact -> menu.previousPage()
        menu.rightReact -> menu.nextPage()
        else -> {
            EmbedBuilder().apply {
                val action = menu.customReactions[emoji] ?: return@with

                message.asMessage().embeds.first().apply(this)
                action.invoke(this)
                menu.updatePage(this)
            }
        }
    }

    message.edit {
        embed = newEmbed
    }
}
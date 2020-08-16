@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.menu

import com.gitlab.kordlib.common.entity.*
import com.gitlab.kordlib.core.*
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.behavior.edit
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.kordx.emoji.*
import com.gitlab.kordlib.kordx.emoji.DiscordEmoji
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.utils.InternalLogger

/**
 * Type-safe builder for creating paginated embeds with custom reactions.
 *
 * @property leftReact Reaction used to move to the previous page.
 * @property rightReact Reaction used to move to the next page.
 */
class MenuDSL {
    private val pages = mutableListOf<EmbedBuilder.() -> Unit>()
    private val reactions = mutableMapOf<ReactionEmoji, EmbedBuilder.() -> Unit>()
    var leftReact = Emojis.arrowLeft
    var rightReact = Emojis.arrowRight

    /**
     * Add a new page to this menu using the EmbedDSL.
     */
    fun page(construct: EmbedBuilder.() -> Unit) {
        pages.add(construct)
    }

    /**
     * Add a reaction to the menu and the action to execute when it is clicked.
     */
    fun reaction(reaction: DiscordEmoji.Generic, action: EmbedBuilder.() -> Unit) {
        reactions[reaction.toReaction()] = action
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
data class Menu(val pages: MutableList<EmbedBuilder.() -> Unit>,
                val customReactions: MutableMap<ReactionEmoji, EmbedBuilder.() -> Unit>,
                val leftReact: ReactionEmoji,
                val rightReact: ReactionEmoji) {
    internal suspend fun build(channel: MessageChannel) {
        if (channel.type == ChannelType.DM)
            return InternalLogger.error("Cannot use menus within a private context.")

        if (pages.isEmpty())
            return InternalLogger.error("A menu must have at least one page.")

        val message = channel.createEmbed {
            pages.first().invoke(this)
        }

        val multiPage = pages.size != 1

        if (multiPage) {
            message.addReaction(leftReact)
            message.addReaction(rightReact)
        }

        customReactions.keys.forEach { message.addReaction(it) }

        if (multiPage || customReactions.isNotEmpty())
            registerReactionListener(message.kord, this, message.id)
    }
}

/** @suppress DSL Builder */
@BuilderDSL
fun menu(construct: MenuDSL.() -> Unit): Menu {
    val handle = MenuDSL()
    handle.construct()
    return handle.build()
}

private suspend fun registerReactionListener(kord: Kord, menu: Menu, menuId: Snowflake) = kord.on<ReactionAddEvent> {
    var index = 0

    if (messageId != menuId) return@on

    message.deleteReaction(user.id, emoji)

    when (emoji) {
        menu.leftReact -> {
            if (index != 0)
                index--
            else
                index = menu.pages.lastIndex
        }

        menu.rightReact -> {
            if (index != menu.pages.lastIndex)
                index++
            else
                index = 0
        }

        else -> {
            val action = menu.customReactions[emoji] ?: return@on
            menu.pages[index] = action
        }
    }

    message.edit {
        menu.pages[index].invoke(this.embed!!)
    }
}
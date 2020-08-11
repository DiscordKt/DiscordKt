@file:Suppress("unused")

package me.jakejmattson.discordkt.api.dsl.menu

import me.jakejmattson.discordkt.api.dsl.embed.*
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.utils.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.EventListener

/**
 * Type-safe builder for creating paginated embeds with custom reactions.
 *
 * @property leftReact Reaction used to move to the previous page.
 * @property rightReact Reaction used to move to the next page.
 */
class MenuDSL {
    private var embeds = mutableListOf<MessageEmbed>()
    private var reactions = hashMapOf<String, ReactionAction>()
    var leftReact: String = "⬅"
    var rightReact: String = "➡"

    /**
     * Add a new page to this menu using the EmbedDSL.
     */
    fun page(construct: EmbedDSL.() -> Unit) {
        val handle = EmbedDSL()
        handle.construct()
        embeds.add(handle.build())
    }

    /**
     * Add a reaction to the menu and the action to execute when it is clicked.
     */
    fun reaction(reaction: String, action: ReactionAction) {
        reactions[reaction] = action
    }

    internal fun build() = Menu(embeds, leftReact, rightReact, reactions)
}

/**
 * Class for storing menu data.
 *
 * @property pages A list of embed representing menu pages.
 * @property leftReact Reaction used to move to the previous page.
 * @property rightReact Reaction used to move to the next page.
 * @property customReactions All custom reactions and their actions.
 */
data class Menu(val pages: MutableList<MessageEmbed>,
                val leftReact: String,
                val rightReact: String,
                val customReactions: Map<String, ReactionAction>) {
    internal fun build(channel: MessageChannel) {
        if (channel is PrivateChannel)
            return InternalLogger.error("Cannot use menus within a private context.")

        if (pages.isEmpty())
            return InternalLogger.error("A menu must have at least one page.")

        channel.sendMessage(pages.first()).queue { message ->
            val multiPage = pages.size != 1

            if (multiPage) {
                message.addReaction(leftReact).queue()
                message.addReaction(rightReact).queue()
            }

            customReactions.keys.forEach { message.addReaction(it).queue() }

            if (multiPage || customReactions.isNotEmpty())
                channel.jda.addEventListener(ReactionListener(message, this))
        }
    }
}

/** @suppress DSL Builder */
@BuilderDSL
fun menu(construct: MenuDSL.() -> Unit): Menu {
    val handle = MenuDSL()
    handle.construct()
    return handle.build()
}

private class ReactionListener(message: Message, private val menu: Menu) : EventListener {
    private var index = 0
    private val messageId = message.id

    override fun onEvent(event: GenericEvent) {
        if (event !is GuildMessageReactionAddEvent)
            return

        if (event.member.user.isBot) return
        if (event.messageId != messageId) return

        event.reaction.removeReaction(event.member.user).queue()

        val reactionString = event.reaction.reactionEmote.emoji

        fun editEmbed() = event.channel.editMessageById(event.messageId, menu.pages[index]).queue()

        when (reactionString) {
            menu.leftReact -> {
                if (index != 0) {
                    index--
                    editEmbed()
                    return
                }
            }

            menu.rightReact -> {
                if (index != menu.pages.lastIndex) {
                    index++
                    editEmbed()
                    return
                }
            }
        }

        val action = menu.customReactions[reactionString] ?: return
        val exposedBuilder = menu.pages[index].toEmbedBuilder()

        action.invoke(exposedBuilder)
        menu.pages[index] = exposedBuilder.build()
        editEmbed()
    }
}
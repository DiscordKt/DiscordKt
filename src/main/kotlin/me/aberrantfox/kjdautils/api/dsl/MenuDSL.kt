package me.aberrantfox.kjdautils.api.dsl

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.EventListener

private const val leftArrow = "⬅"
private const val rightArrow = "➡"

class MenuDSLHandle {
    private var embeds: MutableList<MessageEmbed> = mutableListOf()

    fun embed(construct: EmbedDSLHandle.() -> Unit) {
        val handle = EmbedDSLHandle()
        handle.construct()
        embeds.add(handle.build())
    }

    fun build() = Menu(embeds)
}

class Menu(val embeds: MutableList<MessageEmbed>)

fun menu(construct: MenuDSLHandle.() -> Unit): Menu {
    val handle = MenuDSLHandle()
    handle.construct()
    return handle.build()
}

fun CommandEvent.respond(menu: Menu) {
    require(menu.embeds.isNotEmpty()) { "Cannot build a menu with no embeds." }

    val firstPage = menu.embeds.first()

    channel.sendMessage(firstPage).queue {
        it.addReaction(leftArrow).queue()
        it.addReaction(rightArrow).queue()
        discord.jda.addEventListener(ReactionListener(it, menu))
    }
}

class ReactionListener(message: Message, private val menu: Menu): EventListener {
    private var index = 0
    private val messageId = message.id

    override fun onEvent(event: GenericEvent) {
        if (event !is GuildMessageReactionAddEvent)
            return

        if (event.member.user == event.jda.selfUser)
            return

        if (event.messageId != messageId)
            return

        val reaction = event.reaction
        val reactionString = reaction.reactionEmote.emoji

        reaction.removeReaction(event.member.user).queue()

        fun editEmbed() {
            event.channel.editMessageById(event.messageId, menu.embeds[index]).queue()
        }

        if (reactionString == leftArrow) {
            if (index != 0) {
                index--
                editEmbed()
            }
        }

        if (reactionString == rightArrow) {
            if (index != menu.embeds.lastIndex) {
                index++
                editEmbed()
            }
        }
    }
}
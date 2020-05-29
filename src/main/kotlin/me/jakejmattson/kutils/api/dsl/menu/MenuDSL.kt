package me.jakejmattson.kutils.api.dsl.menu

import me.aberrantfox.kutils.api.dsl.command.CommandEvent
import me.aberrantfox.kutils.api.dsl.embed.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.EventListener

typealias ReactionAction = (currentEmbed: EmbedBuilder) -> Unit

class MenuDSLHandle {
    private var embeds: MutableList<MessageEmbed> = mutableListOf()
    private var reactions: HashMap<String, ReactionAction> = hashMapOf()
    var leftReact: String = "⬅"
    var rightReact: String = "➡"

    fun embed(construct: EmbedDSLHandle.() -> Unit) {
        val handle = EmbedDSLHandle()
        handle.construct()
        embeds.add(handle.build())
    }

    fun reaction(reaction: String, action: ReactionAction) {
        reactions[reaction] = action
    }

    fun build() = Menu(embeds, leftReact, rightReact, reactions)
}

data class Menu(val embeds: MutableList<MessageEmbed>,
                val leftReact: String,
                val rightReact: String,
                val customReactions: HashMap<String, ReactionAction>)

fun menu(construct: MenuDSLHandle.() -> Unit): Menu {
    val handle = MenuDSLHandle()
    handle.construct()
    return handle.build()
}

fun CommandEvent<*>.respond(menu: Menu) {
    require(menu.embeds.isNotEmpty()) { "Cannot build a menu with no embeds." }

    val firstPage = menu.embeds.first()

    if (menu.embeds.size == 1) {
        channel.sendMessage(firstPage).queue()
        return
    }

    channel.sendMessage(firstPage).queue { message ->
        message.addReaction(menu.leftReact).queue()
        message.addReaction(menu.rightReact).queue()

        menu.customReactions.keys.forEach {
            message.addReaction(it).queue()
        }

        discord.jda.addEventListener(ReactionListener(message, menu))
    }
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

        fun editEmbed() = event.channel.editMessageById(event.messageId, menu.embeds[index]).queue()

        when (reactionString) {
            menu.leftReact -> {
                if (index != 0) {
                    index--
                    editEmbed()
                    return
                }
            }

            menu.rightReact -> {
                if (index != menu.embeds.lastIndex) {
                    index++
                    editEmbed()
                    return
                }
            }
        }

        val action = menu.customReactions[reactionString] ?: return
        val exposedBuilder = menu.embeds[index].toEmbedBuilder()

        action.invoke(exposedBuilder)
        menu.embeds[index] = exposedBuilder.build()
        editEmbed()
    }
}
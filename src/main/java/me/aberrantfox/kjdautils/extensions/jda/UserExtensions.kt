package me.aberrantfox.hotbot.extensions.jda

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User


fun User.toMember(guild: Guild) = guild.getMemberById(this.id)

fun User.sendPrivateMessage(msg: MessageEmbed) =
    openPrivateChannel().queue {
        it.sendMessage(msg).queue()
    }

fun User.sendPrivateMessage(msg: String) =
    openPrivateChannel().queue {
        it.sendMessage(msg).queue()
    }

fun User.fullName() = "$name#$discriminator"

fun User.descriptor() = "${fullName()} :: ID :: $id"
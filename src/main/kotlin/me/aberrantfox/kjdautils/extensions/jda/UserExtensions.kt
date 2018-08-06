package me.aberrantfox.kjdautils.extensions.jda

import me.aberrantfox.kjdautils.internal.logging.BotLogger
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User


fun User.toMember(guild: Guild) = guild.getMemberById(this.id)

fun User.sendPrivateMessage(msg: MessageEmbed, log: BotLogger) =
        openPrivateChannel().queue {
            it.sendMessage(msg).queue(null) {
                notifyDirectMessageError(this.fullName(), log)
            }
        }

fun User.sendPrivateMessage(msg: String, log: BotLogger) =
        openPrivateChannel().queue {
            it.sendMessage(msg).queue(null) {
                notifyDirectMessageError(this.fullName(), log)
        }
    }

fun User.fullName() = "$name#$discriminator"

fun User.descriptor() = "${fullName()} :: ID :: $id"

fun notifyDirectMessageError(user: String, log: BotLogger) = log.alert("Couldn't open private chat with $user since the user doesn't allow direct messages from server members.")
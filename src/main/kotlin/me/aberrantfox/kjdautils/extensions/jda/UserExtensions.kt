package me.aberrantfox.kjdautils.extensions.jda

import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User


fun User.toMember(guild: Guild) = guild.getMemberById(this.id)

fun User.sendPrivateMessage(msg: MessageEmbed, log: BotLogger = DefaultLogger()) =
        openPrivateChannel().queue {
            it.sendMessage(msg).queue(null) {
                notifyDirectMessageError(this.fullName(), log)
            }
        }

fun User.sendPrivateMessage(msg: String, log: BotLogger = DefaultLogger()) =
        openPrivateChannel().queue {
            it.sendMessage(msg).queue(null) {
                notifyDirectMessageError(this.fullName(), log)
            }
        }

fun User.fullName() = "$name#$discriminator"

fun User.descriptor() = "${fullName()} :: ID :: $id"

private fun notifyDirectMessageError(user: String, log: BotLogger) = log.alert("Couldn't open private chat with $user since the user doesn't allow direct messages from server members.")
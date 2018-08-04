package me.aberrantfox.kjdautils.extensions.jda

import me.aberrantfox.kjdautils.internal.logging.BotLogger
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User


fun User.toMember(guild: Guild) = guild.getMemberById(this.id)

fun User.sendPrivateMessage(msg: MessageEmbed, log: BotLogger) =
        try {
            openPrivateChannel().queue {
                it.sendMessage(msg).complete()
            }
        }
        catch (e: Exception) {
            log.alert("Couldn't open private chat with ${this.fullName()} since the user doesn't allow direct messages from server members.")
        }

fun User.sendPrivateMessage(msg: String, log: BotLogger) =
        try {
            openPrivateChannel().queue {
                it.sendMessage(msg).complete()
            }
        }
        catch (e: Exception) {
            log.alert("Couldn't open private chat with ${this.fullName()} since the user doesn't allow direct messages from server members.")
        }

fun User.fullName() = "$name#$discriminator"

fun User.descriptor() = "${fullName()} :: ID :: $id"
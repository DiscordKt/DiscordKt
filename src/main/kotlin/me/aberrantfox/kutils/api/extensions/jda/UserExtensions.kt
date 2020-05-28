@file:Suppress("unused")

package me.aberrantfox.kutils.api.extensions.jda

import me.aberrantfox.kutils.internal.utils.InternalLogger
import net.dv8tion.jda.api.entities.*

fun User.toMember(guild: Guild) = guild.getMemberById(this.id)

fun User.sendPrivateMessage(msg: MessageEmbed) =
    openPrivateChannel().queue {
        it.sendMessage(msg).queue(null) {
            InternalLogger.error(fullName())
        }
    }

fun User.sendPrivateMessage(msg: String) =
    openPrivateChannel().queue {
        it.sendMessage(msg).queue(null) {
            notifyDirectMessageError(fullName())
        }
    }

fun User.fullName() = "$name#$discriminator"

fun User.descriptor() = "${fullName()} :: ID :: $id"

private fun notifyDirectMessageError(user: String) = InternalLogger.error("Failed to send private message to $user")
@file:Suppress("unused")

package me.aberrantfox.kutils.api.extensions.jda

import me.aberrantfox.kutils.api.extensions.stdlib.*
import net.dv8tion.jda.api.entities.Message

fun Message.containsInvite() = contentRaw.containsInvite()

fun Message.containsURL() = contentRaw.containsURl()

fun Message.deleteIfExists(runnable: () -> Unit = {}) = channel.retrieveMessageById(id).queue { it?.delete()?.queue { runnable() } }

fun Message.mentionsSomeone() = (mentionsEveryone() || mentionedUsers.size > 0 || mentionedRoles.size > 0)

fun Message.isImagePost() =
    if (attachments.isNotEmpty()) {
        attachments.first().isImage && contentRaw.isBlank()
    } else {
        false
    }

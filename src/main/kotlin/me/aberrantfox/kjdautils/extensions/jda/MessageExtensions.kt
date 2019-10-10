package me.aberrantfox.kjdautils.extensions.jda

import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.extensions.stdlib.*
import net.dv8tion.jda.api.entities.Message

fun Message.containsInvite() = contentRaw.containsInvite()

fun Message.containsURL() = contentRaw.containsURl()

fun Message.isCommandInvocation(config: KConfiguration) = contentRaw.startsWith(config.prefix)

fun Message.deleteIfExists(runnable: () -> Unit = {}) = channel.retrieveMessageById(id).queue { it?.delete()?.queue { runnable() } }

fun Message.isDoubleInvocation(prefix: String) = contentRaw.startsWith(prefix + prefix)

fun Message.mentionsSomeone() = (mentionsEveryone() || mentionedUsers.size > 0 || mentionedRoles.size > 0)

fun Message.isImagePost() =
    if(attachments.isNotEmpty()) {
        attachments.first().isImage && contentRaw.isBlank()
    } else {
        false
    }

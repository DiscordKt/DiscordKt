package me.aberrantfox.hotbot.extensions.jda

import me.aberrantfox.kjdautils.extensions.stdlib.containsInvite
import me.aberrantfox.kjdautils.extensions.stdlib.containsURl
import me.aberrantfox.hotbot.services.Configuration
import net.dv8tion.jda.core.entities.Message

fun Message.containsInvite() = contentRaw.containsInvite()

fun Message.containsURL() = contentRaw.containsURl()

fun Message.isCommandInvocation(config: Configuration) = contentRaw.startsWith(config.serverInformation.prefix)

fun Message.deleteIfExists(runnable: () -> Unit = {}) = channel.getMessageById(id).queue { it?.delete()?.queue { runnable() } }

fun Message.mentionsSomeone() = (mentionsEveryone() || mentionedUsers.size > 0 || mentionedRoles.size > 0)

fun Message.isImagePost() =
    if(attachments.isNotEmpty()) {
        attachments.first().isImage && contentRaw.isNullOrBlank()
    } else {
        false
    }

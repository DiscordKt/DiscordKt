@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.jda

import me.jakejmattson.kutils.api.extensions.stdlib.*
import net.dv8tion.jda.api.entities.Message

/**
 * Checks whether or not this message's raw content contains an invite.
 */
fun Message.containsInvite() = contentRaw.containsInvite()

/**
 * Checks whether or not this message's raw content contains a URL.
 */
fun Message.containsURL() = contentRaw.containsURl()

/**
 * Checks whether or not this message's raw content mentions a user or role.
 */
fun Message.mentionsSomeone() = (mentionsEveryone() || mentionedUsers.size > 0 || mentionedRoles.size > 0)

/**
 * Checks whether or not this message has an attached image and has no text.
 */
fun Message.isImagePost() =
    if (attachments.isNotEmpty()) {
        attachments.first().isImage && contentRaw.isBlank()
    } else {
        false
    }

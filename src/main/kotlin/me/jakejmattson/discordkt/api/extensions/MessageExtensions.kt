@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import com.gitlab.kordlib.core.entity.Message
import kotlinx.coroutines.flow.count

/**
 * Checks whether or not this message's raw content contains an invite.
 */
fun Message.containsInvite() = content.containsInvite()

/**
 * Checks whether or not this message's raw content contains a URL.
 */
fun Message.containsURL() = content.containsURl()

/**
 * Checks whether or not this message's raw content mentions a user or role.
 */
suspend fun Message.mentionsSomeone() = (mentionsEveryone || mentionedUsers.count() > 0 || mentionedRoles.count() > 0)

/**
 * Checks whether or not this message has an attached image and has no text.
 */
fun Message.isImagePost() =
    if (attachments.isNotEmpty()) {
        attachments.first().isImage && content.isBlank()
    } else {
        false
    }

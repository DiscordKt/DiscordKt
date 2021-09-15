@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import kotlinx.coroutines.flow.count

/**
 * Checks whether this message's raw content contains an invite.
 */
fun Message.containsInvite() = content.containsInvite()

/**
 * Checks whether this message's raw content contains a URL.
 */
fun Message.containsURL() = content.containsURl()

/**
 * Checks whether this message's raw content mentions a user or role.
 */
suspend fun Message.mentionsSomeone() = (mentionsEveryone || mentionedUsers.count() > 0 || mentionedRoles.count() > 0)

/**
 * Determine the Discord URL for this message (or null if in a DM).
 */
suspend fun Message.jumpLink() = getGuildOrNull()?.let { "https://discord.com/channels/${it.id.value}/${channel.id.value}/${id.value}" }

/**
 * Checks whether this message has an attached image and has no text.
 */
fun Message.isImagePost() =
    if (attachments.isNotEmpty()) {
        attachments.first().isImage && content.isBlank()
    } else {
        false
    }

/**
 * Add multiple [ReactionEmoji] to a [Message].
 */
suspend fun Message.addReactions(vararg reactions: ReactionEmoji) = reactions.forEach { addReaction(it) }

/**
 * Add multiple [ReactionEmoji] to a [Message].
 */
suspend fun Message.addReactions(reactions: List<ReactionEmoji>) = reactions.forEach { addReaction(it) }

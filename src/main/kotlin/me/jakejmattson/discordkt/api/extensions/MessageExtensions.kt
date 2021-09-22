@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import kotlinx.coroutines.flow.count

/**
 * Checks if this message contains a Discord invite.
 */
public fun Message.containsInvite(): Boolean = content.containsInvite()

/**
 * Checks if this message contains a URL.
 */
public fun Message.containsURL(): Boolean = content.containsURl()

/**
 * Checks if this message mentions a user or role.
 */
public suspend fun Message.mentionsSomeone(): Boolean = (mentionsEveryone || mentionedUsers.count() > 0 || mentionedRoles.count() > 0)

/**
 * Determine the Discord URL for this message (null if in a DM).
 */
public suspend fun Message.jumpLink(): String? = getGuildOrNull()?.let { "https://discord.com/channels/${it.id.value}/${channel.id.value}/${id.value}" }

/**
 * Checks if this message is exclusively an image.
 */
public fun Message.isImagePost(): Boolean =
    if (attachments.isNotEmpty()) {
        attachments.first().isImage && content.isBlank()
    } else {
        false
    }

/**
 * Add multiple [ReactionEmoji] to a [Message].
 */
public suspend fun Message.addReactions(vararg reactions: ReactionEmoji): Unit = reactions.forEach { addReaction(it) }

/**
 * Add multiple [ReactionEmoji] to a [Message].
 */
public suspend fun Message.addReactions(reactions: List<ReactionEmoji>): Unit = reactions.forEach { addReaction(it) }

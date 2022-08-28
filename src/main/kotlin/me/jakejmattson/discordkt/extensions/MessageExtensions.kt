@file:Suppress("unused")

package me.jakejmattson.discordkt.extensions

import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.create.allowedMentions
import kotlinx.coroutines.flow.count

/**
 * Checks if this message contains a Discord invite.
 */
public fun Message.containsInvite(): Boolean = content.containsInvite()

/**
 * Find all invites in this message.
 */
public fun Message.getInvites(): List<String> = content.getInvites()

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
public fun Message.isImagePost(): Boolean = content.isBlank() && attachments.firstOrNull()?.isImage == true

/**
 * Add multiple [ReactionEmoji] to a [Message].
 */
public suspend fun Message.addReactions(vararg reactions: ReactionEmoji): Unit = reactions.forEach { addReaction(it) }

/**
 * Add multiple [ReactionEmoji] to a [Message].
 */
public suspend fun Message.addReactions(reactions: List<ReactionEmoji>): Unit = reactions.forEach { addReaction(it) }

/**
 * Reply to this message with no mentions allowed.
 */
public suspend fun Message.replySilently(builder: suspend UserMessageCreateBuilder.() -> Unit): Message = reply {
    allowedMentions()
    builder.invoke(this)
}

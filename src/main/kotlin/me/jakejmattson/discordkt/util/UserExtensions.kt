@file:Suppress("unused")

package me.jakejmattson.discordkt.util

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.rest.Image
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

/**
 * Get guilds shared between the user and the bot.
 */
public val UserBehavior.mutualGuilds: Flow<Guild>
    get() = kord.guilds.filter { it.getMemberOrNull(id) != null }

/**
 * A Discord profile link for this user.
 */
public val UserBehavior.profileLink: String
    get() = "https://discord.com/users/${id.value}/"

/**
 * A link to this user's profile picture (or default).
 */
public val User.pfpUrl: String
    get() = avatar?.cdnUrl?.toUrl() ?: defaultPfpUrl

/**
 * A link to this user's default profile picture.
 */
public val User.defaultPfpUrl: String
    get() = defaultAvatar.cdnUrl.toUrl { format = Image.Format.PNG }

/**
 * A User's name and discriminator
 * username#1234
 */
@Deprecated("Without a discriminator, this is not needed", ReplaceWith("username"))
public val User.fullName: String
    get() = username

/**
 * Send a private message to a user if possible.
 *
 * @return The [Message] object or null if you cannot DM.
 */
public suspend fun UserBehavior.sendPrivateMessage(message: Any = "", embed: (suspend EmbedBuilder.() -> Unit)? = null): Message? =
    getDmChannelOrNull()?.createMessage {
        val responseContent = message.toString()

        if (responseContent.isNotEmpty())
            content = responseContent

        if (embed != null)
            embed { embed.invoke(this) }
    }

/**
 * Checks if this [User] is itself.
 */
public fun UserBehavior.isSelf(): Boolean = id == kord.selfId

/**
 * User entity formatted to a readable String.
 * username :: <@username>
 */
public fun User.descriptor(): String = "$username :: $mention"

/**
 * User entity formatted to a readable String.
 * <@username> (username)
 */
public fun User.simpleDescriptor(): String = "$mention ($username)"

/**
 * User entity formatted to a readable String.
 * username :: 123456789123456789
 */
public fun User.idDescriptor(): String = "$username :: ${id.value}"
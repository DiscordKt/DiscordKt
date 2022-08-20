@file:Suppress("unused")

package me.jakejmattson.discordkt.extensions

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
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
 * A link to this user's profile picture.
 */
public val User.pfpUrl: String
    get() = avatar?.url ?: defaultAvatar.url

/**
 * A User's name and discriminator
 * username#1234
 */
public val User.fullName: String
    get() = "$username#$discriminator"

/**
 * Send the user a private string message.
 */
public suspend fun UserBehavior.sendPrivateMessage(message: String): Message = getDmChannel().createMessage(message)

/**
 * Send the user a private embed message.
 */
public suspend fun UserBehavior.sendPrivateMessage(embed: suspend EmbedBuilder.() -> Unit): Message = getDmChannel().createEmbed { embed.invoke(this) }

/**
 * Checks if this [User] is itself.
 */
public fun UserBehavior.isSelf(): Boolean = id == kord.selfId

/**
 * User entity formatted to a readable String.
 * username#1234 :: <@username>
 */
public fun User.descriptor(): String = "$fullName :: $mention"

/**
 * User entity formatted to a readable String.
 * <@username> (username#1234)
 */
public fun User.simpleDescriptor(): String = "$mention ($fullName)"

/**
 * User entity formatted to a readable String.
 * username#1234 :: 123456789123456789
 */
public fun User.idDescriptor(): String = "$fullName :: ${id.value}"
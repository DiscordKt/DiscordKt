@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.filter

/**
 * Send the user a private string message.
 */
suspend fun UserBehavior.sendPrivateMessage(message: String) = getDmChannel().createMessage(message)

/**
 * Send the user a private embed message.
 */
suspend fun UserBehavior.sendPrivateMessage(embed: suspend EmbedBuilder.() -> Unit) = getDmChannel().createEmbed { embed.invoke(this) }

/**
 * Get guilds shared between the user and the bot.
 */
val UserBehavior.mutualGuilds
    get() = kord.guilds.filter { it.getMemberOrNull(id) != null }

/**
 * A Discord profile link for this user.
 */
val UserBehavior.profileLink
    get() = "https://discord.com/users/${id.value}/"

/**
 * Checks if this [User] is itself.
 */
fun UserBehavior.isSelf() = id == kord.selfId

/**
 * Checks if this [Member][MemberBehavior] is itself.
 */
fun MemberBehavior.isSelf() = id == kord.selfId

/**
 * User entity formatted to a readable String.
 * username#1234 :: <@username>
 */
fun User.descriptor() = "$username#$discriminator :: $mention"

/**
 * User entity formatted to a readable String.
 * <@username> (username#1234)
 */
fun User.simpleDescriptor() = "$mention ($username#$discriminator)"

/**
 * User entity formatted to a readable String.
 * username#1234 :: 123456789123456789
 */
fun User.idDescriptor() = "$username#$discriminator :: ${id.value}"
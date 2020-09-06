@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder

/**
 * Send the user a private string message.
 */
suspend fun User.sendPrivateMessage(message: String) = getDmChannel().createMessage(message)

/**
 * Send the user a private embed message.
 */
suspend fun User.sendPrivateMessage(embed: suspend EmbedBuilder.() -> Unit) = getDmChannel().createEmbed { embed.invoke(this) }

/**
 * A Discord profile link for this user.
 */
val User.profileLink
    get() = "https://discordapp.com/users/$id/"
@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions.jda

import com.gitlab.kordlib.core.entity.User

/**
 * Send the user a private string message.
 */
suspend fun User.sendPrivateMessage(msg: String) = getDmChannel().createMessage(msg)

/**
 * A Discord profile link for this user.
 */
val User.profileLink
    get() = "https://discordapp.com/users/$id/"
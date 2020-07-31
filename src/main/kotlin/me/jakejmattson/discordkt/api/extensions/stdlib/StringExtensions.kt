@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions.stdlib

import me.jakejmattson.discordkt.api.extensions.jda.fullName
import net.dv8tion.jda.api.JDA

private val urlRegexes = listOf(
    "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
).map { it.toRegex() }

private val inviteRegex = "(\n|.)*((discord|discordapp).(gg|me|io|com/invite)/)(\n|.)*".toRegex()

/**
 * Whether ot not this string matches a URL regex.
 */
fun String.containsURl() = urlRegexes.any { replace("\n", "").contains(it) }

/**
 * Whether or not this string matches the invite regex.
 */
fun String.containsInvite() = inviteRegex.matches(this)

/**
 * Whether or not this string is a valid boolean value (true/false/t/f).
 */
fun String.isBooleanValue() =
    when (this.toLowerCase()) {
        "true" -> true
        "false" -> true
        "t" -> true
        "f" -> true
        else -> false
    }

/**
 * Sanitize all mentions and replace them with their resolved discord names.
 */
fun String.sanitiseMentions(jda: JDA) = this
    .split(" ")
    .filter { it.startsWith("<") && it.endsWith(">") }
    .map { mention ->
        val id = mention.trimToID()

        val name = when (mention[1]) {
            '@' -> jda.retrieveUserById(id).complete()?.fullName()
            '#' -> jda.getGuildChannelById(id)?.name
            '&' -> jda.getRoleById(id)?.name
            else -> null
        } ?: id

        mention to name
    }.foldRight(this) { mentionMap: Pair<String, String>, result: String ->
        result.replace(mentionMap.first, mentionMap.second)
    }

/**
 * Trim any type of mention into an ID.
 */
fun String.trimToID() =
    if (startsWith("<") && endsWith(">"))
        this.replace("<", "")
            .replace(">", "")
            .replace("@", "") // User mentions
            .replace("!", "") // User mentions with nicknames
            .replace("&", "") // Role mentions
            .replace("#", "") // Channel mentions
    else
        this
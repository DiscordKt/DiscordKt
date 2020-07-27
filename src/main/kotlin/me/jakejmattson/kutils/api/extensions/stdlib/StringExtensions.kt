@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.stdlib

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
fun String.isBooleanValue(): Boolean =
    when (this.toLowerCase()) {
        "true" -> true
        "false" -> true
        "t" -> true
        "f" -> true
        else -> false
    }

/**
 * Remove the @ symbol from this String.
 */
fun String.sanitiseMentions() = this.replace("@", "")

/**
 * Trim any type of mention into an ID.
 */
fun String.trimToID(): String =
    if (this.startsWith("<") && this.endsWith(">")) {
        replace("<", "")
            .replace(">", "")
            .replace("@", "")
            .replace("!", "") // User mentions with nicknames
            .replace("&", "") // Role mentions
            .replace("#", "") // Channel mentions
    } else {
        this
    }
@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import com.gitlab.kordlib.common.entity.Snowflake
import me.jakejmattson.discordkt.api.Discord

private val urlRegexes = listOf(
    "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
).map { it.toRegex() }

private val inviteRegex = "(\n|.)*((discord|discordapp).(gg|me|io|com/invite)/)(\n|.)*".toRegex()
private val roleRegex = "<@&(\\d+)>".toRegex()
private val userRegex = "<@!?(\\d+)>".toRegex()
private val hereRegex = "@+here".toRegex()
private val everyoneRegex = "@+everyone".toRegex()

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
    when (toLowerCase()) {
        "true" -> true
        "false" -> true
        "t" -> true
        "f" -> true
        else -> false
    }

/**
 * Sanitize all mentions and replace them with their resolved discord names.
 */
suspend fun String.sanitiseMentions(discord: Discord) = cleanseRoles(discord)
    .cleanseUsers(discord)
    .cleanseHere()
    .cleanseEveryone()
    .cleanseAll()

/**
 * Trim any type of mention into an ID.
 */
fun String.trimToID() = takeUnless { startsWith("<") && endsWith(">") }
    ?: replaceAll(listOf("<", ">", "@", "!", "&", "#").zip(listOf("", "", "", "", "", "")))

/**
 * Convert an ID or mention to a Snowflake.
 */
fun String.toSnowflake() = Snowflake(trimToID())

private fun String.replaceAll(replacements: List<Pair<String, String>>): String {
    var result = this
    replacements.forEach { (l, r) -> result = result.replace(l, r) }
    return result
}

private suspend fun String.cleanseRoles(discord: Discord): String {
    val roleMentions = roleRegex.findAll(this).map {
        val mention = it.value

        val name = discord.api
        val resolvedName = "" //TODO Figure out roles

        mention to resolvedName
    }.toList()

    return replaceAll(roleMentions)
}

private suspend fun String.cleanseUsers(discord: Discord): String {
    val userMentions = userRegex.findAll(this).map {
        val mention = it.value
        val name = "" //TODO Figure out suspends; discord.kord.getUser(mention.trimToSnowflake())?.tag ?: ""

        mention to name
    }.toList()

    return replaceAll(userMentions)
}

private fun String.cleanseHere(): String {
    val mentions = hereRegex.findAll(this).map { it.value to "here" }.toList()
    return replaceAll(mentions)
}

private fun String.cleanseEveryone(): String {
    val mentions = everyoneRegex.findAll(this).map { it.value to "everyone" }.toList()
    return replaceAll(mentions)
}

private fun String.cleanseAll(): String {
    val remaining = everyoneRegex.findAll(this).count() + hereRegex.findAll(this).count()
    return takeUnless { remaining != 0 } ?: replace("@", "")
}
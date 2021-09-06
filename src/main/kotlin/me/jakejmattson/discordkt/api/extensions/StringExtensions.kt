@file:Suppress("unused")

package me.jakejmattson.discordkt.api.extensions

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.Discord

private val urlRegexes = listOf(
    "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
).map { it.toRegex() }

private val inviteRegex = "(discord\\.(gg|me|io|com/invite)/)([^\\s]+)".toRegex()
private val roleRegex = "<@&(\\d+)>".toRegex()
private val userRegex = "<@!?(\\d+)>".toRegex()
private val hereRegex = "@+here".toRegex()
private val everyoneRegex = "@+everyone".toRegex()

/**
 * Whether this string matches a URL regex.
 */
fun String.containsURl() = urlRegexes.any { replace("\n", "").contains(it) }

/**
 * Whether this string matches the invite regex.
 */
fun String.containsInvite() = inviteRegex.matches(this)

/**
 * Whether this string is a valid boolean value (true/false/t/f).
 */
fun String.isBooleanValue() =
    when (lowercase()) {
        "true", "t" -> true
        "false", "f" -> true
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

private fun String.replaceAll(replacements: List<Pair<String, String>>): String {
    var result = this
    replacements.forEach { (l, r) -> result = result.replace(l, r) }
    return result
}

private suspend fun String.cleanseRoles(discord: Discord): String {
    val roleMentions = roleRegex.findAll(this).map {
        runBlocking {
            val mention = it.value
            val roles = discord.kord.guilds.toList().flatMap { it.roles.toList() }.associate { it.mention to it.name }
            val resolvedName = roles[mention] ?: ""

            mention to resolvedName
        }
    }.toList()

    return replaceAll(roleMentions)
}

private suspend fun String.cleanseUsers(discord: Discord): String {
    val userMentions = userRegex.findAll(this).map {
        runBlocking {
            val mention = it.value
            val replacement = mention.toSnowflakeOrNull()?.let { discord.kord.getUser(it)?.tag } ?: ""

            mention to replacement
        }
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
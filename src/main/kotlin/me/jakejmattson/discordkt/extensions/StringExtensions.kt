@file:Suppress("unused")

package me.jakejmattson.discordkt.extensions

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord

private val urlRegexes = listOf(
    "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
).map { it.toRegex() }

private val inviteRegex = "(https?://)?(www\\.)?(discord\\.(gg|me|io|com/invite)/)([^\\s]+)".toRegex()
private val roleRegex = "<@&(\\d+)>".toRegex()
private val userRegex = "<@!?(\\d+)>".toRegex()
private val hereRegex = "@+here".toRegex()
private val everyoneRegex = "@+everyone".toRegex()

public fun MutableList<String>.consumeFirst(): String = if (this.isNotEmpty()) this.removeFirst() else ""

/**
 * Whether this string matches a URL regex.
 * @sample me.jakejmattson.discordkt.extensions.urlRegexes
 */
public fun String.containsURl(): Boolean = urlRegexes.any { replace("\n", "").contains(it) }

/**
 * Whether this string matches the invite regex.
 * @sample me.jakejmattson.discordkt.extensions.inviteRegex
 */
public fun String.containsInvite(): Boolean = inviteRegex.containsMatchIn(this)

/**
 * Find all invites in this string.
 */
public fun String.getInvites(): List<String> = inviteRegex.findAll(this).map { it.value }.toList()

/**
 * Whether this string is a valid boolean value (true/false/t/f).
 */
public fun String.isBooleanValue(): Boolean =
    when (lowercase()) {
        "true", "t" -> true
        "false", "f" -> true
        else -> false
    }

/**
 * Sanitize all mentions and replace them with their resolved discord names.
 */
public suspend fun String.sanitiseMentions(discord: Discord): String = cleanseRoles(discord)
    .cleanseUsers(discord)
    .cleanseHere()
    .cleanseEveryone()
    .cleanseAll()

/**
 * Trim any type of mention into an ID.
 */
public fun String.trimToID(): String = takeUnless { startsWith("<") && endsWith(">") }
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

@file:Suppress("unused")

package me.jakejmattson.discordkt.extensions

import dev.kord.core.entity.*
import dev.kord.core.entity.channel.GuildMessageChannel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.commands.Command
import java.awt.Color

private val urlRegexes = listOf(
    "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
).map { it.toRegex() }

private val inviteRegex = "(https?://)?(www\\.)?(discord\\.(gg|me|io|com/invite)/)([^\\s]+)".toRegex()
private val roleRegex = "<@&(\\d+)>".toRegex()
private val userRegex = "<@!?(\\d+)>".toRegex()
private val hereRegex = "@+here".toRegex()
private val everyoneRegex = "@+everyone".toRegex()

/**
 * Remove and return the first element in a mutable list.
 */
public fun MutableList<String>.consumeFirst(): String = if (this.isNotEmpty()) this.removeFirst() else ""

/**
 * Whether this string matches a URL regex.
 * @sample me.jakejmattson.discordkt.extensions.urlRegexes
 */
public fun String.containsURl(): Boolean = urlRegexes.any { replace("\n", "").contains(it) }

/**
 * Whether this string contains a discord invite.
 * @sample me.jakejmattson.discordkt.extensions.inviteRegex
 */
public fun String.containsInvite(): Boolean = inviteRegex.containsMatchIn(this)

/**
 * Return all discord invites in this string.
 * @sample me.jakejmattson.discordkt.extensions.inviteRegex
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

/**
 * Convert any generic type into a more readable String.
 *
 * @param entity The entity to be converted.
 */
public fun <T> stringify(entity: T): String =
    when (entity) {
        //Discord entities
        is GuildMessageChannel -> entity.id.toString()
        is Attachment -> entity.filename
        is Guild -> entity.id.toString()
        is Role -> entity.id.toString()
        is User -> entity.id.toString()

        //DiscordKt
        is Command -> entity.name
        is Success<*> -> stringify(entity.result)
        is Error<*> -> entity.error

        //Standard Library
        is Color -> with(entity) { "#%02X%02X%02X".format(red, green, blue) }

        else -> entity.toString()
    }
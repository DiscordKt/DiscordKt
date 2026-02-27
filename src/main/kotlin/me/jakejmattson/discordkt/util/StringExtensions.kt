package me.jakejmattson.discordkt.util

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.GuildChannel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.commands.Command
import java.awt.Color
import java.util.*

/**
 * Remove and return the first element in a mutable list.
 */
public fun MutableList<String>.consumeFirst(): String = if (this.isNotEmpty()) this.removeFirst() else ""

/**
 * Generate a random [UUID] String.
 */
public fun uuid(): String = UUID.randomUUID().toString()

/**
 * The 3 snowflake elements of a message link.
 * @param guildId The [Guild] snowflake
 * @param channelId The [GuildChannel] snowflake
 * @param messageId The [Message] snowflake
 */
public data class MessageParts(val guildId: Snowflake, val channelId: Snowflake, val messageId: Snowflake)

/**
 * Unwrap a message link into its [MessageParts].
 */
public fun String.unwrapMessageLink(): MessageParts? {
    val match = DiscordRegex.publicMessage.find(this)?.groupValues ?: return null
    return MessageParts(Snowflake(match[1]), Snowflake(match[2]), Snowflake(match[3]))
}

/**
 * Whether this string matches a URL regex.
 * @sample me.jakejmattson.discordkt.util.DiscordRegex.url
 */
public fun String.containsURl(): Boolean = DiscordRegex.url.any { replace("\n", "").contains(it) }

/**
 * Whether this string contains a discord invite.
 * @sample me.jakejmattson.discordkt.util.DiscordRegex.invite
 */
public fun String.containsInvite(): Boolean = DiscordRegex.invite.containsMatchIn(this)

/**
 * Return all discord invites in this string.
 * @sample me.jakejmattson.discordkt.util.DiscordRegex.invite
 */
public fun String.getInvites(): List<String> = DiscordRegex.invite.findAll(this).map { it.value }.toList()

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
public fun String.sanitiseMentions(discord: Discord): String = cleanseRoles(discord)
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

private fun String.cleanseRoles(discord: Discord): String {
    val roleMentions = DiscordRegex.role.findAll(this).map {
        runBlocking {
            val mention = it.value
            val roles = discord.kord.guilds.toList().flatMap { it.roles.toList() }.associate { it.mention to it.name }
            val resolvedName = roles[mention] ?: ""

            mention to resolvedName
        }
    }.toList()

    return replaceAll(roleMentions)
}

private fun String.cleanseUsers(discord: Discord): String {
    val userMentions = DiscordRegex.user.findAll(this).map {
        runBlocking {
            val mention = it.value
            val replacement = mention.toSnowflakeOrNull()?.let { discord.kord.getUser(it)?.username } ?: ""

            mention to replacement
        }
    }.toList()

    return replaceAll(userMentions)
}

private fun String.cleanseHere(): String {
    val mentions = DiscordRegex.here.findAll(this).map { it.value to "here" }.toList()
    return replaceAll(mentions)
}

private fun String.cleanseEveryone(): String {
    val mentions = DiscordRegex.everyone.findAll(this).map { it.value to "everyone" }.toList()
    return replaceAll(mentions)
}

private fun String.cleanseAll(): String {
    val remaining = DiscordRegex.everyone.findAll(this).count() + DiscordRegex.here.findAll(this).count()
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
        is GuildChannel -> entity.id.toString()
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
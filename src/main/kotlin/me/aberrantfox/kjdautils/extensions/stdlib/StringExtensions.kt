package me.aberrantfox.kjdautils.extensions.stdlib

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User

private val urlRegexes = listOf(
    "[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)",
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)"
).map { it.toRegex() }

private val inviteRegex = "(\n|.)*((discord|discordapp).(gg|me|io|com/invite)/)(\n|.)*".toRegex()

fun String.containsURl() = urlRegexes.any { this.replace("\n", "").contains(it) }

fun String.containsInvite() = inviteRegex.matches(this)

fun String.formatJdaDate() = this.substring(0, this.indexOf("T"))

fun String.limit(length: Int) = if (this.length > length) substring(length) else this

fun String.isInteger(): Boolean =
        try {
            this.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }

fun String.isLong(): Boolean =
        try {
            this.toLong()
            true
        } catch (e: NumberFormatException) {
            false
        }

fun String.isDouble(): Boolean =
        try {
            this.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }

fun String.isBooleanValue(): Boolean =
        when(this.toLowerCase()) {
            "true" -> true
            "false" -> true
            "t" -> true
            "f" -> true
            else -> false
        }

fun String.toBooleanValue(): Boolean =
        when(this.toLowerCase()) {
            "true" -> true
            "t" -> true
            else -> false
        }

fun String.idToName(jda: JDA): String = jda.getUserById(this).name

fun String.idToUser(jda: JDA): User = jda.getUserById(this.trimToID())

fun String.toRole(guild: Guild): Role? = guild.getRoleById(this)

fun String.sanitiseMentions() = this.replace("@", "")

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
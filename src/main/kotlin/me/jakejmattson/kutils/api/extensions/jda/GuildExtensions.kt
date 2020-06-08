@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.jda

import me.jakejmattson.kutils.api.extensions.stdlib.formatJdaDate
import net.dv8tion.jda.api.entities.*

fun Guild.getMemberJoinString(target: User) =
    if (isMember(target)) {
        target.toMember(this)!!.timeJoined.toString().formatJdaDate()
    } else {
        "This user is not currently in this guild"
    }

fun Guild.getRoleByName(name: String, ignoreCase: Boolean = true) = getRolesByName(name, ignoreCase).firstOrNull()

fun Guild.getRoleByIdOrName(idOrName: String): Role? {
    if (idOrName.toLowerCase() == "everyone") {
        return this.publicRole
    }
    val name = getRoleByName(idOrName, true)

    if (name != null) return name

    return try {
        getRoleById(idOrName)
    } catch (e: NumberFormatException) {
        null
    }
}

fun Guild.hasRole(roleName: String): Boolean = this.roles.any { it.name.toLowerCase() == roleName.toLowerCase() }
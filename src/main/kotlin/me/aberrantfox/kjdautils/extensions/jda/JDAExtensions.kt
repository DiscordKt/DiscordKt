package me.aberrantfox.kjdautils.extensions.jda

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User

enum class RoleIdentifier {
    Name, Id, Invalid
}

fun JDA.getRoleIdentifier(role: String): RoleIdentifier {
    val roles = guilds.map { it.roles }.flatten()

    if (roles.any { it.id == role }) return RoleIdentifier.Id

    if (roles.any { it.name.toLowerCase() == role.toLowerCase() }) return RoleIdentifier.Name

    return RoleIdentifier.Invalid
}

fun JDA.isRole(role: String) = guilds.map { it.roles }.flatten().any { it.id == role || it.name.toLowerCase() == role.toLowerCase() }


fun JDA.obtainRole(role: String): Role? = guilds.map { it.roles }.flatten().firstOrNull { it.name.toLowerCase() == role.toLowerCase() || it.id == role }


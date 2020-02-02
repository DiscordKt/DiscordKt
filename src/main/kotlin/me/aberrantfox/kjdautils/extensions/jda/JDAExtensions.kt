package me.aberrantfox.kjdautils.extensions.jda

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Role

enum class RoleIdentifier {
    Name, Id, Invalid
}

fun JDA.getRoleIdentifier(role: String): RoleIdentifier {
    val roles = roles

    if (roles.any { it.id == role }) return RoleIdentifier.Id

    if (roles.any { it.name.toLowerCase() == role.toLowerCase() }) return RoleIdentifier.Name

    return RoleIdentifier.Invalid
}

fun JDA.isRole(role: String) = roles.any { it.id == role || it.name.toLowerCase() == role.toLowerCase() }


fun JDA.obtainRole(role: String): Role? = roles.firstOrNull { it.name.toLowerCase() == role.toLowerCase() || it.id == role }

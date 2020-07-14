@file:Suppress("unused")

package me.jakejmattson.kutils.api.extensions.jda

import net.dv8tion.jda.api.entities.*

fun Guild.getRoleByIdOrName(idOrName: String): Role? {
    if (idOrName.toLowerCase() == "everyone")
        return publicRole
    
    val name = getRolesByName(name, true).firstOrNull()

    if (name != null) return name

    return try {
        getRoleById(idOrName)
    } catch (e: NumberFormatException) {
        null
    }
}
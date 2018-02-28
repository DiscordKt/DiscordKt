package me.aberrantfox.hotbot.extensions.jda

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.User

fun JDA.isRole(role: String) = this.getRolesByName(role, true).size == 1
fun JDA.performActionIfIsID(id: String, action: (User) -> Unit) =
    retrieveUserById(id).queue {
        action(it)
    }
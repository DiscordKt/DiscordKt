package me.aberrantfox.hotbot.extensions.jda

import net.dv8tion.jda.core.entities.Member


fun Member.fullName() = "${this.user.name}#${this.user.discriminator}"

fun Member.descriptor() = "${fullName()} :: ID :: ${this.user.id}"

fun Member.getHighestRole() =
    if(roles.isNotEmpty()) {
        roles.maxBy { it.position }
    } else {
        guild.roles.minBy { it.position }
    }
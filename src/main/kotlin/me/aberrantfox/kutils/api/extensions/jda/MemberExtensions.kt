@file:Suppress("unused")

package me.aberrantfox.kutils.api.extensions.jda

import net.dv8tion.jda.api.entities.Member

fun Member.fullName() = "${this.user.name}#${this.user.discriminator}"

fun Member.descriptor() = "${this.user.name}#${this.user.discriminator} :: ID :: ${this.user.id}"

fun Member.getHighestRole() =
    if (roles.isNotEmpty()) {
        roles.maxBy { it.position }
    } else {
        guild.roles.minBy { it.position }
    }
package me.aberrantfox.kutils.api.extensions.jda

import net.dv8tion.jda.api.entities.Role


fun Role.isEqualOrHigherThan(other: Role?) = if (other == null) false else this.position >= other.position